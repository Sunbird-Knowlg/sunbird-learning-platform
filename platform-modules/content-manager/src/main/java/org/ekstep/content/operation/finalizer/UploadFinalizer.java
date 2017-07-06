package org.ekstep.content.operation.finalizer;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ExtractionType;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.util.ContentPackageExtractionUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class UploadFinalizer, extends BaseFinalizer which
 * mainly holds common methods and operations of a ContentBody.
 * UploadFinalizer holds methods which perform ContentuploadPipeline operations
 */
public class UploadFinalizer extends BaseFinalizer {
	
	/** The logger. */
	private static ILogger LOGGER = new PlatformLogger(PublishFinalizer.class.getName());
	
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
		
		LOGGER.log("Started fetching the Parameters from Parameter Map.");
		
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
		LOGGER.log("Generated ECML String From ECRF: " , ecml);

		// Upload Package
		String folderName = S3PropertyReader.getProperty(s3Artifact);
		String[] urlArray = uploadToAWS(file, getUploadFolderName(contentId, folderName));
		LOGGER.log("Package Uploaded to S3.");
		
		// Extract Content Uploaded Package to S3
		ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
		contentPackageExtractionUtil.extractContentPackage(contentId, node, file, ExtractionType.snapshot);

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
		LOGGER.log("Content Body Update Status: " , response.getResponseCode());

		// Update Node
		response = updateContentNode(contentId, node, urlArray[IDX_S3_URL]);
		LOGGER.log("Content Node Update Status: " , response.getResponseCode());
		
		if (!checkError(response))
			response.put(GraphDACParams.node_id.name(), contentId);
		
		try {
			LOGGER.log("Deleting the temporary folder: " , basePath);
			delete(new File(basePath));
		} catch (Exception e) {
			LOGGER.log("Error deleting the temporary folder: " , basePath, e);
		}
		
		return response;
	}

}
