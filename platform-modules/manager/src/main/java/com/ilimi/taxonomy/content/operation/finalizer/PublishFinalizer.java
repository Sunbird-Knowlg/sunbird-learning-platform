package com.ilimi.taxonomy.content.operation.finalizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;
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
 * PublishFinalizer holds methods which perform ContentPublishPipeline operations
 */
public class PublishFinalizer extends BaseFinalizer {
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(PublishFinalizer.class.getName());
	
	/** The Constant IDX_S3_KEY. */
	private static final int IDX_S3_KEY = 0;
	
	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;
	
	/** The BasePath. */
	protected String basePath;
	
	/** The ContentId. */
	protected String contentId;

	/**
	 * PublishFinalizer()
	 * sets the basePath and ContentId
	 *
	 * @param BasePath the basePath
	 * @param contentId the contentId
	 * checks if the basePath is valid else throws ClientException
	 * checks if the ContentId is not null else throws ClientException
	 */
	public PublishFinalizer(String basePath, String contentId) {
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
	 * checks if Node, ecrfType,ecmlType
	 * exists in the parameterMap else throws ClientException
	 * Output only ECML format
	 * create 'artifactUrl'
	 * Get Content String
	 * write ECML File
	 * Create 'ZIP' Package
	 * Upload Package
	 * Upload to S3
	 * Set artifact file For Node
	 * Download App Icon and create thumbnail
	 * Set Package Version
	 * Create ECAR Bundle
	 * Delete local compressed artifactFile
	 * Populate Fields and Update Node
	 * @return the response
	 */	
	public Response finalize(Map<String, Object> parameterMap) {
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
			writeECMLFile(basePath, ecml, ecmlType);
			// Create 'ZIP' Package
			String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + contentId
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
					+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
			createZipPackage(basePath, zipFileName);
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
		createThumbnail(basePath, node);
		
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
		Map<Object, List<String>> downloadUrls = contentBundle.createContentManifestData(ctnts, childrenIds, null);
		String[] urlArray = contentBundle.createContentBundle(ctnts, bundleFileName, ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls);

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
		return updateContentNode(newNode, urlArray[IDX_S3_URL]);
	}
	
}
