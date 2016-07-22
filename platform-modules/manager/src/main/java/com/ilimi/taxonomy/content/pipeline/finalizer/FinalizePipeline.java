package com.ilimi.taxonomy.content.pipeline.finalizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.optimizr.ThumbnailGenerator;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.ZipUtility;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.operation.finalizer.BundleFinalizer;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;
import com.ilimi.taxonomy.util.ContentBundle;

public class FinalizePipeline extends BasePipeline {

	private static Logger LOGGER = LogManager.getLogger(FinalizePipeline.class.getName());

	private static final int IDX_S3_KEY = 0;
	private static final int IDX_S3_URL = 1;

	protected String basePath;
	protected String contentId;

	public FinalizePipeline(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	public Response finalyze(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && !StringUtils.isBlank(operation)) {
			switch (operation) {
				case "upload":
				case "UPLOAD": {
						response = finalizeUpload(parameterMap);
					}
					break;
					
				case "publish":
				case "PUBLISH": {
						response = finalizePublish(parameterMap);
					}
					break;
					
				case "bundle":
				case "BUNDLE": {
						BundleFinalizer bundleFinalizer = new BundleFinalizer(basePath, contentId);
						response = bundleFinalizer.finalize(parameterMap);
					}
					break;
					
				default:
					break;
			}
		}
		try {
			FileUtils.deleteDirectory(new File(basePath));
		} catch (Exception e) {
			LOGGER.error("Error deleting directory: " + basePath, e);
		}
		return response;
	}
	
	private Response finalizeUpload(Map<String, Object> parameterMap) {
		Response response = new Response();
		File file = (File) parameterMap.get(ContentWorkflowPipelineParams.file.name());
		Plugin ecrf = (Plugin) parameterMap.get(ContentWorkflowPipelineParams.ecrf.name());
		String ecmlType = (String) parameterMap.get(ContentWorkflowPipelineParams.ecmlType.name());
		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		if (null == file || !file.exists())
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [File does not Exist.]");
		if (null == ecrf)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM
							+ " | [Invalid or null ECRF Object.]");
		if (StringUtils.isBlank(ecmlType))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid ECML Type.]");
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");

		// Get Content String
		String ecml = getECMLString(ecrf, ContentWorkflowPipelineParams.ecml.name());

		// Upload Package
		String[] urlArray = uploadToAWS(file, getUploadFolderName());

		// Update Body, Reset Editor State and Update Content Node
		node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), urlArray[IDX_S3_KEY]);
		node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
		node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), ecml);
		node.getMetadata().put(ContentWorkflowPipelineParams.editorState.name(), null);

		// Update Node
		response = updateContentNode(node, urlArray[IDX_S3_URL]);
		return response;
	}
	
	private Response finalizePublish(Map<String, Object> parameterMap) {
		Response response = new Response();
		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		Plugin ecrf = (Plugin) parameterMap.get(ContentWorkflowPipelineParams.ecrf.name());
		// Output only ECML format
		String ecmlType = ContentWorkflowPipelineParams.ecml.name();
		boolean isCompressionApplied = (boolean) parameterMap
				.get(ContentWorkflowPipelineParams.isCompressionApplied.name());
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");
		if (null == ecrf)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM
							+ " | [Invalid or null ECRF Object.]");
		LOGGER.info("Compression Applied ? " + isCompressionApplied);
		// Create 'artifactUrl' Package
		String artifactUrl = null;
		if (BooleanUtils.isTrue(isCompressionApplied)) {
			// Get Content String
			String ecml = getECMLString(ecrf, ecmlType);
			// Write ECML File
			writeECMLFile(ecml, ecmlType);
			// Create 'ZIP' Package
			String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + contentId
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
					+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
			createZipPackage(zipFileName);
			// Upload Package
			File packageFile = new File(zipFileName);
			if (packageFile.exists()) {
				// Upload to S3
				String[] urlArray = uploadToAWS(packageFile, getUploadFolderName());
				if (null != urlArray && urlArray.length >= 2)
					artifactUrl = urlArray[IDX_S3_URL];
				
				// Set artifact file For Node
				node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), packageFile);
			}
		}
		// Download App Icon and create thumbnail
		createThumbnail(node);
		
		// Set Package Version
		double version = 1.0;
		if (null != node && null != node.getMetadata()
				&& null != node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()))
			version = getDoubleValue(node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()))
					+ 1;
		node.getMetadata().put(ContentWorkflowPipelineParams.pkgVersion.name(), version);

		// Create ECAR Bundle
		List<Node> nodes = new ArrayList<Node>();
		node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Live.name());
		nodes.add(node);
		List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
		List<String> childrenIds = new ArrayList<String>();
		getContentBundleData(node.getGraphId(), nodes, ctnts, childrenIds);
		String bundleFileName = Slug
				.makeSlug((String) node.getMetadata().get(ContentWorkflowPipelineParams.name.name()), true)
				+ "_" + System.currentTimeMillis() + "_" + node.getIdentifier() + ".ecar";
		ContentBundle contentBundle = new ContentBundle();
		String[] urlArray = contentBundle.createContentBundle(ctnts, childrenIds, bundleFileName, "1.1");

		// Delete local compressed artifactFile
		Object artifact = node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
		if (null != artifact && artifact instanceof File) {
			File packageFile = (File) artifact;
			if (packageFile.exists())
				packageFile.delete();
			LOGGER.info("Deleting Local Artifact Package File: " + packageFile.getAbsolutePath());
			node.getMetadata().remove(ContentWorkflowPipelineParams.artifactUrl.name());
			
			if (StringUtils.isNotBlank(artifactUrl))
				node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
		}
		
		// Populate Fields and Update Node
		node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), urlArray[IDX_S3_KEY]);
		node.getMetadata().put(ContentWorkflowPipelineParams.downloadUrl.name(), urlArray[IDX_S3_URL]);
		node.getMetadata().put(ContentWorkflowPipelineParams.lastPublishedOn.name(), formatCurrentDate());
		node.getMetadata().put(ContentWorkflowPipelineParams.size.name(), getS3FileSize(urlArray[IDX_S3_KEY]));
		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());
		response = updateContentNode(newNode, urlArray[IDX_S3_URL]);
		return response;
	}

	private void createZipPackage(String zipFileName) {
		if (!StringUtils.isBlank(zipFileName)) {
			LOGGER.info("Creating Zip File: " + zipFileName);
			ZipUtility appZip = new ZipUtility(basePath, zipFileName);
			appZip.generateFileList(new File(basePath));
			appZip.zipIt(zipFileName);
		}
	}

	private void writeECMLFile(String ecml, String ecmlType) {
		try {
			if (StringUtils.isBlank(ecml))
				throw new ClientException(ContentErrorCodeConstants.EMPTY_ECML.name(),
						ContentErrorMessageConstants.EMPTY_ECML_STRING + " | [Unable to write Empty ECML File.]");
			if (StringUtils.isBlank(ecmlType))
				throw new ClientException(ContentErrorCodeConstants.INVALID_ECML_TYPE.name(),
						ContentErrorMessageConstants.INVALID_ECML_TYPE
								+ " | [System is in a fix between (XML & JSON) ECML Type.]");
			
			LOGGER.info("ECML File Type: " + ecmlType);
			File file = new File(basePath + File.separator + ContentConfigurationConstants.DEFAULT_ECML_FILE_NAME
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR + ecmlType);
			LOGGER.info("Creating ECML File With Name: " + file.getAbsolutePath());
			FileUtils.writeStringToFile(file, ecml);
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ECML_FILE_WRITE.name(),
					ContentErrorMessageConstants.ECML_FILE_WRITE_ERROR + " | [Unable to Write ECML File.]");
		}
	}

	private void createThumbnail(Node node) {
		try {
			if (null != node) {
				String appIcon = (String) node.getMetadata().get(ContentWorkflowPipelineParams.appIcon.name());
				if (!StringUtils.isBlank(appIcon)) {
					LOGGER.info("Content Id: " + node.getIdentifier() + " | App Icon: " + appIcon);
					File appIconFile = HttpDownloadUtility.downloadFile(appIcon, basePath);
					if (null != appIconFile && appIconFile.exists() && appIconFile.isFile()) {
						boolean generated = ThumbnailGenerator.generate(appIconFile);
						if (generated) {
							String thumbnail = appIconFile.getParent() + File.separator 
									+ FilenameUtils.getBaseName(appIconFile.getPath()) + ".thumb." 
									+ FilenameUtils.getExtension(appIconFile.getPath());
							File thumbFile = new File(thumbnail);
							if (thumbFile.exists()) {
								LOGGER.info("Thumbnail created for Content Id: " + node.getIdentifier());
								String[] urlArray = uploadToAWS(thumbFile, getUploadFolderName());
								if (null != urlArray && urlArray.length >= 2) {
									String thumbUrl = urlArray[IDX_S3_URL];
									node.getMetadata().put(ContentWorkflowPipelineParams.appIcon.name(), thumbUrl);
									node.getMetadata().put(ContentWorkflowPipelineParams.posterImage.name(), appIcon);
								}
								try {
									thumbFile.delete();
									LOGGER.info("Deleted local Thumbnail file");
								} catch (Exception e) {
								}
							}
						}
						try {
							appIconFile.delete();
							LOGGER.info("Deleted local AppIcon file");
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
					ContentErrorMessageConstants.APP_ICON_DOWNLOAD_ERROR
							+ " | [Unable to Download App Icon for Content Id: '" + node.getIdentifier() + "' ]",
					e);
		}
	}

}
