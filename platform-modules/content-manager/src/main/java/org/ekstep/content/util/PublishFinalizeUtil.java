package org.ekstep.content.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.ekstep.common.Slug;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.entity.Manifest;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.operation.finalizer.BaseFinalizer;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.contentstore.ContentStore;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

public class PublishFinalizeUtil extends BaseFinalizer{
	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
	private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";
	private static final List<String> contentFrameworkMetafields = Arrays.asList("organisationBoardIds", "organisationSubjectIds", 
			"organisationMediumids", "organisationTopicsIds", "organisationGradeLevelIds", "targetBoardIds", "targetSubjectIds", 
			"targetMediumIds", "targetTopicIds", "targetGradeLevelIds");
	
	private ContentStore contentStore = new ContentStore();
	ControllerUtil controllerUtil = new ControllerUtil();
	
	public PublishFinalizeUtil(ContentStore contentStore) {
		this.contentStore = contentStore;
	}
	public PublishFinalizeUtil(ControllerUtil controllerUtil) {
		this.controllerUtil = controllerUtil;
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
	
	public Map<String, List<String>> mergeOrganisationAndtargetFrameworks(Node node) {
		Map<String, List<String>> frameworkMetadata = new HashMap<String, List<String>>();
		String[] defaultArray = {};
		Map<String, Object> metaData = node.getMetadata();
		String organisationFrameworkId = (String)metaData.get("organisationFrameworkId");
		if(StringUtils.isNotBlank(organisationFrameworkId))
			frameworkMetadata.put("se_frameworkIds", mergeIds(Arrays.asList(organisationFrameworkId), 
					Arrays.asList((String[])metaData.getOrDefault("targetFrameworkIds", defaultArray))));
		else
			frameworkMetadata.put("se_frameworkIds", mergeIds(new ArrayList<String>(), 
					Arrays.asList((String[])metaData.getOrDefault("targetFrameworkIds", defaultArray))));
		frameworkMetadata.put("se_boardIds", mergeIds(Arrays.asList((String[])metaData.getOrDefault("organisationBoardIds", defaultArray)), 
				Arrays.asList((String[])metaData.getOrDefault("targetBoardIds", defaultArray))));
		frameworkMetadata.put("se_subjectIds", mergeIds(Arrays.asList((String[])metaData.getOrDefault("organisationSubjectIds", defaultArray)), 
				Arrays.asList((String[])metaData.getOrDefault("targetSubjectIds", defaultArray))));
		frameworkMetadata.put("se_mediumIds", mergeIds(Arrays.asList((String[])metaData.getOrDefault("organisationMediumids", defaultArray)), 
				Arrays.asList((String[])metaData.getOrDefault("targetMediumIds", defaultArray))));
		frameworkMetadata.put("se_topicIds", mergeIds(Arrays.asList((String[])metaData.getOrDefault("organisationTopicsIds", defaultArray)), 
				Arrays.asList((String[])metaData.getOrDefault("targetTopicIds", defaultArray))));
		frameworkMetadata.put("se_gradeLevelIds", mergeIds(Arrays.asList((String[])metaData.getOrDefault("organisationGradeLevelIds", defaultArray)), 
				Arrays.asList((String[])metaData.getOrDefault("targetGradeLevelIds", defaultArray))));
		
		Map<String, List<String>> frameworkMetafieldsLabel = getLabels(metaData, node.getIdentifier());
		if(MapUtils.isNotEmpty(frameworkMetafieldsLabel)) {
			frameworkMetadata.put("se_boards", mergeIds((List<String>)frameworkMetafieldsLabel.getOrDefault("organisationBoardIds", new ArrayList<String>()), 
					(List<String>)frameworkMetafieldsLabel.getOrDefault("targetBoardIds", new ArrayList<String>())));
			frameworkMetadata.put("se_subjects", mergeIds((List<String>)frameworkMetafieldsLabel.getOrDefault("organisationSubjectIds", new ArrayList<String>()), 
					(List<String>)frameworkMetafieldsLabel.getOrDefault("targetSubjectIds", new ArrayList<String>())));
			frameworkMetadata.put("se_mediums", mergeIds((List<String>)frameworkMetafieldsLabel.getOrDefault("organisationMediumids", new ArrayList<String>()), 
					(List<String>)frameworkMetafieldsLabel.getOrDefault("targetMediumIds", new ArrayList<String>())));
			frameworkMetadata.put("se_topics", mergeIds((List<String>)frameworkMetafieldsLabel.getOrDefault("organisationTopicsIds", new ArrayList<String>()), 
					(List<String>)frameworkMetafieldsLabel.getOrDefault("targetTopicIds", new ArrayList<String>())));
			frameworkMetadata.put("se_gradeLevels", mergeIds((List<String>)frameworkMetafieldsLabel.getOrDefault("organisationGradeLevelIds", new ArrayList<String>()), 
					(List<String>)frameworkMetafieldsLabel.getOrDefault("targetGradeLevelIds", new ArrayList<String>())));
		}
		return frameworkMetadata;
	}
	protected List<String> mergeIds(List<String> orgList, List<String> targetList){
		Set<String> mergedList = new HashSet<String>();
		mergedList.addAll(orgList);
		mergedList.addAll(targetList);
		return CollectionUtils.isEmpty(mergedList)? null : new ArrayList<String>(mergedList);
	}
	protected Map<String, List<String>> getLabels(Map<String, Object> metadata, String identifier){
		List<String> ids = new ArrayList<String>();
		String[] defaultArray = {};
		for(String id : contentFrameworkMetafields) 
			ids.addAll(Arrays.asList((String[])metadata.getOrDefault(id, defaultArray)));
		if(CollectionUtils.isEmpty(ids)) {
			TelemetryManager.info("For Content :: " + identifier + " no framework categories are set in metadata.");
			return null;
		}
		Response response = controllerUtil.getDataNodes("domain", ids);
		if (response.getResponseCode() != ResponseCode.OK) {
			TelemetryManager.error("Error while fetching framework related objects:: ResponseCode:: " + response.getResponseCode() + 
					" Error Mesaage:: " + response.getParams().getErrmsg());
			return null;
		}else {
			List<Node> nodes = (List<Node>) response.getResult().get(GraphDACParams.node_list.name());
			if(CollectionUtils.isEmpty(nodes)) {
				TelemetryManager.info("For Content :: " + identifier + " no framework categories object found for ids:: " + ids);
				return null;
			}
			Map<String, Map<String, Object>> nodeMap = nodes.stream().collect(Collectors.toMap(Node:: getIdentifier, Node:: getMetadata));
			Map<String, List<String>> frameworkMetadata = new HashMap<String, List<String>>();
			if(!nodeMap.isEmpty()) {
				for(String metaField : contentFrameworkMetafields) {
					List<String> idList = Arrays.asList((String[])metadata.getOrDefault(metaField, defaultArray));
					if(CollectionUtils.isNotEmpty(idList)) {
						List<String> labelList =  new ArrayList<String>();
						idList.stream().forEach(id -> labelList.add((String)((Map<String, Object>)nodeMap.get(id)).get("name")));
						frameworkMetadata.put(metaField, labelList);
					}
					
				}
			}
			return frameworkMetadata;
		}
	}
}
