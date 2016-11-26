package com.ilimi.taxonomy.content.operation.finalizer;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.S3PropertyReader;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ExtractionType;
import com.ilimi.taxonomy.util.ContentPackageExtractionUtil;

/**
 * The Class UploadFinalizer, extends BaseFinalizer which
 * mainly holds common methods and operations of a ContentBody.
 * UploadFinalizer holds methods which perform ContentuploadPipeline operations
 */
public class UploadFinalizer extends BaseFinalizer {
	
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

	private static final String s3Artifact = "s3.artifact.folder";
	
	/**
	 * Instantiates a new UploadFinalizer and sets the base
	 * path and current content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file handling and all manipulations. 
	 * @param contentId
	 *            the content id is the identifier of content for which the Processor is being processed currently.
	 */
	public UploadFinalizer(String basePath, String contentId) {
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
	 * checks if file,node,ecrfType,ecmlType
	 * exists in the parameterMap else throws ClientException
	 * Get Content String
	 * Upload Package
	 * Update Body, Reset Editor State and Update Content Node
	 * Update Node 
	 * @return the response
	 */	
	public Response finalize(Map<String, Object> parameterMap) {
		Response response = new Response();
		
		LOGGER.info("Started fetching the Parameters from Parameter Map.");
		
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
		LOGGER.info("Generated ECML String From ECRF: " + ecml);

		// Upload Package
		String folderName = S3PropertyReader.getProperty(s3Artifact);
		String[] urlArray = uploadToAWS(file, getUploadFolderName(node.getIdentifier(), folderName));
		LOGGER.info("Package Uploaded to S3.");
		
		// Extract Content Uploaded Package to S3
		ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
		contentPackageExtractionUtil.extractContentPackage(node, file, ExtractionType.snapshot);

		// Update Body, Reset Editor State and Update Content Node
		node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), urlArray[IDX_S3_KEY]);
		node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
		node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), ecml);
		node.getMetadata().put(ContentWorkflowPipelineParams.editorState.name(), null);

		// Update Node
		response = updateContentNode(node, urlArray[IDX_S3_URL]);
		LOGGER.info("Content Node Update Status: " + response.getResponseCode());
		
		return response;
	}

}
