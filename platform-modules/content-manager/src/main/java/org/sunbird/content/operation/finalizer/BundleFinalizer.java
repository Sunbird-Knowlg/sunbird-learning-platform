package org.sunbird.content.operation.finalizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Slug;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.common.EcarPackageType;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.util.ContentBundle;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.telemetry.logger.TelemetryManager;


/**
 * The Class BundleFinalizer, extends BaseFinalizer which
 * mainly holds common methods and operations of a ContentBody.
 * BundleFinalizer holds methods which perform ContentBundlePipeline operations
 */
public class BundleFinalizer extends BaseFinalizer {

	/** The logger. */
	
	
	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;

	/** The BasePath. */
	protected String basePath;
	
	/** The ContentId. */
	protected String contentId;
	
    private static final String BUNDLE_FOLDER = "cloud_storage.bundle.folder";

	/**
	 * Instantiates a new BundleFinalizer and sets the base
	 * path and current content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file handling and all manipulations. 
	 * @param contentId
	 *            the content id is the identifier of content for which the Processor is being processed currently.
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
	 * @param parameterMap the parameterMap
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
	@SuppressWarnings("unchecked")
	public Response finalize(Map<String, Object> parameterMap) {
		Response response = new Response();
		Map<String, Object> bundleMap = (Map<String, Object>) parameterMap
				.get(ContentWorkflowPipelineParams.bundleMap.name());
		List<Map<String, Object>> contents = (List<Map<String, Object>>)(parameterMap.get(ContentWorkflowPipelineParams.Contents.name()));
		List<String> childrenIds = (List<String>)(parameterMap.get(ContentWorkflowPipelineParams.children.name()));
		String bundleFileName = basePath + File.separator
				+ (String) parameterMap.get(ContentWorkflowPipelineParams.bundleFileName.name());
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
		TelemetryManager.log("Fetching the Parameters From BundleFinalizer.");
		for (Map<String, Object> contentMap : contents) {
			String contentId = (String) contentMap.get(ContentWorkflowPipelineParams.identifier.name());
			TelemetryManager.log("Processing Content Id: " + contentId);
			Map<String, Object> nodeMap = (Map<String, Object>) bundleMap.get(contentId);
			if (null == nodeMap)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM
								+ " | [All the Content for Bundling should be Valid, Invalid or null Content Cannnot be Bundled (Content Id - "
								+ contentId + ").]");

			TelemetryManager.log("Fetching the Parameters For Content Id: " + contentId);
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
			TelemetryManager.log("Base Path For Content Id '" + this.contentId + "' is " + this.basePath);
			TelemetryManager.log("Is Compression Applied ? " + isCompressionApplied);

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
				String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + Slug.makeSlug(contentId)
						+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
						+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
				TelemetryManager.log("Zip File Name: " + zipFileName);
				createZipPackage(basePath, zipFileName);
				zipPackages.add(new File(zipFileName));

				// Upload Package
				File packageFile = new File(zipFileName);
				if (packageFile.exists()) {
					// Upload to S3
					String folderName = S3PropertyReader.getProperty(ARTEFACT_FOLDER);
					String[] urlArray = uploadToAWS(packageFile, getUploadFolderName(this.contentId, folderName));
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
		TelemetryManager.log("Bundle Will Expire On: " + expiresOn);
		
		// Update Content data with relative paths
		ContentBundle contentBundle = new ContentBundle();
		contentBundle.createContentManifestData(contents, childrenIds, expiresOn, EcarPackageType.FULL);

		// Create Manifest JSON File
		File manifestFile = new File(basePath + File.separator + ContentWorkflowPipelineParams.manifest.name()
				+ File.separator + ContentConfigurationConstants.CONTENT_BUNDLE_MANIFEST_FILE_NAME);
		contentBundle.createManifestFile(manifestFile, manifestVersion, expiresOn ,contents);
		zipPackages.add(manifestFile);

		// Create ECAR File
		File file = contentBundle.createBundle(zipPackages, bundleFileName);

		// Upload ECAR to S3
		String folderName = S3PropertyReader.getProperty(BUNDLE_FOLDER);
		folderName = folderName + System.currentTimeMillis();
		String[] urlArray = uploadToAWS(file, getUploadFolderName(this.contentId, folderName));
		if (null != urlArray && urlArray.length >= 2)
			response.put(ContentWorkflowPipelineParams.ECAR_URL.name(), urlArray[IDX_S3_URL]);

		try {
			TelemetryManager.log("Deleting the temporary folder: " + basePath);
			delete(new File(basePath).getParentFile());
		} catch (Exception e) {
			TelemetryManager.error("Error deleting the temporary folder: " + basePath, e);
		}
		return response;
	}
}
