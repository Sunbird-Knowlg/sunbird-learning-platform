package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;


/**
 * The Class YoutubeMimeTypeManager is a implementation of IMimeTypeManager for
 * Mime-Type as <code>video/youtube</code> for Content creation.
 * 
 * @author Rashmi
 * 
 * @see IMimeTypeManager
 */
public class YoutubeMimeTypeManager extends BaseMimeTypeManager implements IMimeTypeManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(org.ekstep.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		if(null != node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name())){
			String mimeType = (String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name());
			if(StringUtils.containsIgnoreCase(mimeType, ContentWorkflowPipelineParams.youtube.name())){
				throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(), ContentErrorMessageConstants.FILE_UPLOAD_ERROR + " | Upload operation not supported");
			}
		}
		return null;
	}
	
	@Override
	public Response upload(String contentId, Node node, String fileUrl) {
		node.getMetadata().put(ContentAPIParams.artifactUrl.name(), fileUrl);
		return updateContentNode(contentId, node, fileUrl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(org.ekstep.graph.dac.model
	 * .Node)
	 */
	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {

		Response response = new Response();
		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		PlatformLogger.log("Calling the 'Review' Initializer for Node Id: " + contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		PlatformLogger.log("Review Operation Finished Successfully for Node ID: " , contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			PlatformLogger.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " + contentId);
			response.put(ContentAPIParams.publishStatus.name(), "Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		}
		else {
			PlatformLogger.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " , node.getIdentifier());
			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#review(org.ekstep.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response review(String contentId, Node node, boolean isAsync) {

		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Calling the 'Review' Initializer for Node ID: " , contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}
}
