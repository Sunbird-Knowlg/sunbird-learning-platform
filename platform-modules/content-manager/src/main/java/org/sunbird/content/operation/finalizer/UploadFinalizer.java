package org.sunbird.content.operation.finalizer;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.common.ExtractionType;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.util.ContentPackageExtractionUtil;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.telemetry.logger.TelemetryManager;


/**
 * The Class UploadFinalizer, extends BaseFinalizer which
 * mainly holds common methods and operations of a ContentBody.
 * UploadFinalizer holds methods which perform ContentuploadPipeline operations
 */
public class UploadFinalizer extends BaseFinalizer {
	
	/** The logger. */
	
	
	/** The Constant IDX_S3_KEY. */
	private static final int IDX_S3_KEY = 0;
	
	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;
	
	/** The BasePath. */
	protected String basePath;
	
	/** The ContentId. */
	protected String contentId;

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
	 * @param parameterMap the parameterMap
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
		
		TelemetryManager.log("Started fetching the Parameters from Parameter Map.");
		
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
		TelemetryManager.log("Generated ECML String From ECRF: " + ecml);

		// Upload Package
		String folderName = S3PropertyReader.getProperty(ARTEFACT_FOLDER);
		String[] urlArray = uploadToAWS(file, getUploadFolderName(contentId, folderName));
		TelemetryManager.log("Package Uploaded to S3.");
		
		// Extract Content Uploaded Package to S3
		ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
		contentPackageExtractionUtil.extractContentPackage(contentId, node, file, ExtractionType.snapshot, true);

		// Update Body, Reset Editor State and Update Content Node
		node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), urlArray[IDX_S3_KEY]);
		node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
		node.getMetadata().put(ContentWorkflowPipelineParams.editorState.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.uploadError.name(), null);
		
		// update content body in content store
		response = updateContentBody(node.getIdentifier(), ecml);
		if (checkError(response))
			return response;
		TelemetryManager.log("Content Body Update Status: " + response.getResponseCode());

		// Update Node
		response = updateContentNode(contentId, node, urlArray[IDX_S3_URL]);
		TelemetryManager.log("Content Node Update Status: " + response.getResponseCode());
		
		if (!checkError(response))
			response.put(GraphDACParams.node_id.name(), contentId);
		
		try {
			TelemetryManager.log("Deleting the temporary folder: " + basePath);
			delete(new File(basePath));
		} catch (Exception e) {
			TelemetryManager.error("Error deleting the temporary folder: " + basePath, e);
		}
		
		return response;
	}

}
