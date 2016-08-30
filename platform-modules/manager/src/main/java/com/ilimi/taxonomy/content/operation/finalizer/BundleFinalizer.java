package com.ilimi.taxonomy.content.operation.finalizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.HttpDownloadUtility;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.util.ContentBundle;

/**
 * The Class BundleFinalizer, extends BaseFinalizer which
 * mainly holds common methods and operations of a ContentBody.
 * BundleFinalizer holds methods which perform ContentBundlePipeline operations
 */
public class BundleFinalizer extends BaseFinalizer {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(BaseFinalizer.class.getName());
	
	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;

	/** The BasePath. */
	protected String basePath;
	
	/** The ContentId. */
	protected String contentId;

	/**
	 * BundleFinalizer()
	 * sets the basePath and ContentId
	 *
	 * @param BasePath the basePath
	 * @param contentId the contentId
	 * checks if the basePath is valid else throws ClientException
	 * checks if the ContentId is not null else throws ClientException
	 */
	public BundleFinalizer(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	/**
	 * finalize()
	 *
	 * @param Map the parameterMap
	 * 
	 * checks if BundleMap, BundleFileName, manifestVersion 
	 * exists in the parameterMap else throws ClientException
	 * 
	 * fetch parameters from bundleFinalizer
	 * Output only ECML format
	 * Setting Attribute Value
	 * Download 'appIcon'
	 * Download 'posterImage'
	 * Get Content String
	 * Write ECML File
	 * Create 'ZIP' Package
	 * Upload Package
	 * Upload to S3
	 * Set artifact file For Node
	 * Update ContentNode
	 * Download from 'artifactUrl'
	 * Get Content Bundle Expiry Date
	 * Update Content data with relative paths
	 * Create Manifest JSON File
	 * Create ECAR File
	 * Upload ECAR to S3
	 * @return the response
	 */	
	public Response finalize(Map<String, Object> parameterMap) {
		Response response = new Response();
		Map<String, Object> bundleMap = (Map<String, Object>) parameterMap
				.get(ContentWorkflowPipelineParams.bundleMap.name());
		List<Map<String, Object>> contents = (List<Map<String, Object>>)(parameterMap.get(ContentWorkflowPipelineParams.Contents.name()));
		List<String> childrenIds = (List<String>)(parameterMap.get(ContentWorkflowPipelineParams.children.name()));
		String bundleFileName = (String) parameterMap.get(ContentWorkflowPipelineParams.bundleFileName.name());
		String manifestVersion = (String) parameterMap.get(ContentWorkflowPipelineParams.manifestVersion.name());
		if (null == bundleMap || bundleMap.isEmpty())
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid or null Parameters.]");
		if (StringUtils.isBlank(bundleFileName))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid Bundle File Name.]");
		if (StringUtils.isBlank(manifestVersion))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM
							+ " | [Invalid Content Manifest Version.]");
		
		List<Node> nodes = new ArrayList<Node>();
		List<File> zipPackages = new ArrayList<File>();
		LOGGER.info("Fetching the Parameters From BundleFinalizer.");
		for (Map<String, Object> contentMap : contents) {
			String contentId = (String) contentMap.get(ContentWorkflowPipelineParams.identifier.name());
			LOGGER.info("Processing Content Id: " + contentId);
			Map<String, Object> nodeMap = (Map<String, Object>) bundleMap.get(contentId);
			if (null == nodeMap)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM
								+ " | [All the Content for Bundling should be Valid, Invalid or null Content Cannnot be Bundled (Content Id - "
								+ contentId + ").]");

			LOGGER.info("Fetching the Parameters For Content Id: " + contentId);
			Plugin ecrf = (Plugin) nodeMap.get(ContentWorkflowPipelineParams.ecrf.name());
			Node node = (Node) nodeMap.get(ContentWorkflowPipelineParams.node.name());
			boolean isCompressionApplied = (boolean) nodeMap
					.get(ContentWorkflowPipelineParams.isCompressionApplied.name());
			String path = (String) nodeMap.get(ContentWorkflowPipelineParams.basePath.name());
			// Output only ECML format
			String ecmlType = ContentWorkflowPipelineParams.ecml.name();

			if (null == ecrf)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM
								+ " | [Invalid or null ECRF Object.]");
			if (null == node)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid or null Node.]");
			if (null == path || !isValidBasePath(path))
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Path does not Exist.]");
			
			// Setting Attribute Value
			this.basePath = path;
			this.contentId = node.getIdentifier();
			LOGGER.info("Base Path For Content Id '" + this.contentId + "' is " + this.basePath);
			LOGGER.info("Is Compression Applied ? " + isCompressionApplied);

			// Download 'appIcon'
			String appIcon = (String) node.getMetadata().get(ContentWorkflowPipelineParams.appIcon.name());
			if (HttpDownloadUtility.isValidUrl(appIcon))
				zipPackages.add(HttpDownloadUtility.downloadFile(appIcon, basePath));
			
			// Download 'posterImage'
			String posterImage = (String) node.getMetadata().get(ContentWorkflowPipelineParams.posterImage.name());
			if (HttpDownloadUtility.isValidUrl(posterImage))
				zipPackages.add(HttpDownloadUtility.downloadFile(posterImage, basePath));

			if (BooleanUtils.isTrue(isCompressionApplied)) {
				// Get Content String
				String ecml = getECMLString(ecrf, ecmlType);

				// Write ECML File
				writeECMLFile(basePath, ecml, ecmlType);

				// Create 'ZIP' Package
				String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + contentId
						+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
						+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
				createZipPackage(basePath, zipFileName);
				zipPackages.add(new File(zipFileName));

				// Upload Package
				File packageFile = new File(zipFileName);
				if (packageFile.exists()) {
					// Upload to S3
					String[] urlArray = uploadToAWS(packageFile, getUploadFolderName());
					if (null != urlArray && urlArray.length >= 2) {
						String artifactUrl = urlArray[IDX_S3_URL];
						
						// Set artifact file For Node
						if (StringUtils.isNotBlank(artifactUrl)) {
							node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
							contentMap.put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
						}
					}
				}
				
				// Update Content Node
				Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
				newNode.setGraphId(node.getGraphId());
				newNode.setMetadata(node.getMetadata());
				updateNode(newNode);
			} else {
				// Download From 'artifactUrl'
				String artifactUrl = (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
				if (HttpDownloadUtility.isValidUrl(artifactUrl))
					zipPackages.add(HttpDownloadUtility.downloadFile(artifactUrl, basePath));
			}
			nodes.add(node);
		}
		
		// Get Content Bundle Expiry Date
		String expiresOn = getDateAfter(ContentConfigurationConstants.DEFAULT_CONTENT_BUNDLE_EXPIRES_IN_DAYS);
		LOGGER.info("Bundle Will Expire On: " + expiresOn);
		
		// Update Content data with relative paths
		ContentBundle contentBundle = new ContentBundle();
		contentBundle.createContentManifestData(contents, childrenIds, expiresOn);

		// Create Manifest JSON File
		File manifestFile = new File(basePath + File.separator + ContentWorkflowPipelineParams.manifest.name()
				+ File.separator + ContentConfigurationConstants.CONTENT_BUNDLE_MANIFEST_FILE_NAME);
		contentBundle.createManifestFile(manifestFile, manifestVersion, expiresOn ,contents);
		zipPackages.add(manifestFile);

		// Create ECAR File
		File file = contentBundle.createBundle(zipPackages, bundleFileName);

		// Upload ECAR to S3
		String[] urlArray = uploadToAWS(file, getUploadFolderName());
		if (null != urlArray && urlArray.length >= 2)
			response.put(ContentWorkflowPipelineParams.ECAR_URL.name(), urlArray[IDX_S3_URL]);

		return response;
	}
}
