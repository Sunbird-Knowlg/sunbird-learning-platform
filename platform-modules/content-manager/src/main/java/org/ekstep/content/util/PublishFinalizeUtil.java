package org.ekstep.content.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.ekstep.common.Slug;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.entity.Manifest;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.operation.finalizer.BaseFinalizer;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.contentstore.ContentStore;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.telemetry.logger.TelemetryManager;

public class PublishFinalizeUtil extends BaseFinalizer{
	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
	private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";
	
	private ContentStore contentStore = new ContentStore();
	
	public PublishFinalizeUtil(ContentStore contentStore) {
		this.contentStore = contentStore;
	}
	public PublishFinalizeUtil() {}

	public String uploadFile(String fileUrl, Node node, String basePath) {
    	
		File file = HttpDownloadUtility.downloadFile(fileUrl, basePath + File.separator + "itemset");
		if (null != file) {
			String newFileName = node.getIdentifier() + "_" + System.currentTimeMillis() + "." + FilenameUtils.getExtension(file.getName());
    		boolean renameStatus = file.renameTo(new File(file.getParentFile(),newFileName));
			
			try {
				String folder = S3PropertyReader.getProperty(CONTENT_FOLDER);
				folder = folder + "/" + Slug.makeSlug(node.getIdentifier(), true) + "/" + S3PropertyReader.getProperty(ARTEFACT_FOLDER);
				String[] url = null;
				if(renameStatus)
					url = CloudStore.uploadFile(folder, new File(file.getParent() + "/" + newFileName), true);
				else
					url = CloudStore.uploadFile(folder, file, true);
				return url[1];
			} catch (Throwable e) {
				TelemetryManager.error("Error during uploading question paper pdf file for content:: " + node.getIdentifier() + " Error:: " + e.getStackTrace());
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
	                    "Error during uploading question paper pdf file for content :" + node.getIdentifier()+". Please Try Again After Sometime!");
			} finally {
				try {
					if(null != file)
						delete(file);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public void replaceArtifactUrl(Node node) {
		String artifactBasePath = (String)node.getMetadata().get("artifactBasePath");
		String artifactUrl = (String)node.getMetadata().get("artifactUrl");
		if(StringUtils.contains(artifactUrl, artifactBasePath)) {
			String sourcePath = StringUtils.substring(artifactUrl, artifactUrl.indexOf((artifactBasePath)));
			String destinationPath = StringUtils.replace(sourcePath, artifactBasePath + File.separator, "");
			
			try	{
				CloudStore.copyObjectsByPrefix(sourcePath, destinationPath, false);
				TelemetryManager.log("Copying Objects...DONE | Under: " + destinationPath);
				String newArtifactUrl = StringUtils.replace(artifactUrl, sourcePath, destinationPath);
				node.getMetadata().put("artifactUrl", newArtifactUrl);
				if(StringUtils.isNotBlank((String)node.getMetadata().get("cloudStorageKey"))) {
					String cloudStorageKey = StringUtils.replace((String)node.getMetadata().get("cloudStorageKey"), artifactBasePath + File.separator, "");
					node.getMetadata().put("cloudStorageKey", cloudStorageKey);
				}
				if(StringUtils.isNotBlank((String)node.getMetadata().get("s3Key"))) {
					String s3Key = StringUtils.replace((String)node.getMetadata().get("s3Key"), artifactBasePath + File.separator, "");
					node.getMetadata().put("s3Key", s3Key);
				}
			} catch(Exception e) {
				TelemetryManager.error("Error while copying object by prefix", e);
			}
		}
	}
	
	public void handleAssetWithExternalLink(Plugin ecrf, String contentId) {
		if (null != ecrf) {
			try {
				Manifest manifest = ecrf.getManifest();
				if (null != manifest) {
					List<Media> medias = manifest.getMedias();
					if(CollectionUtils.isNotEmpty(medias)) {
						List<Map<String, Object>> externalLink = new ArrayList<Map<String,Object>>();
						for (Media media: medias) {
							TelemetryManager.log("Validating Asset for External link: " + media.getId());
							if(validateAssetMediaForExternalLink(media)) {
								Map<String, Object> assetMap = new HashMap<String, Object>();
								assetMap.put("id", media.getId());
								assetMap.put("src", media.getSrc());
								assetMap.put("type", media.getType());
								externalLink.add(assetMap);
							}
						}
						contentStore.updateExternalLink(contentId, externalLink);
					}
				}
			}catch(Exception e) {
				TelemetryManager.error("Error while pushing externalLink details of content Id: " + contentId +" into cassandra.", e);
			}
		}
	}
	
	protected boolean validateAssetMediaForExternalLink(Media media){
		boolean isExternal = false;
		UrlValidator validator = new UrlValidator();
		String urlLink = media.getSrc();
		if(StringUtils.isNotBlank(urlLink) && 
				validator.isValid(media.getSrc()) &&
				!StringUtils.contains(urlLink, CloudStore.getContainerName()))
			isExternal = true; 
		return isExternal;
	}
}
