package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class YoutubeMimeTypeManager is a implementation of IMimeTypeManager for
 * Mime-Type as <code>video/youtube</code> for Content creation.
 * 
 * @author Rashmi
 * 
 * @see IMimeTypeManager
 */
public class YoutubeMimeTypeManager extends BaseMimeTypeManager implements IMimeTypeManager {

	/** Logger */
	private static PlatformLogger<YoutubeMimeTypeManager> LOGGER = new PlatformLogger<>(YoutubeMimeTypeManager.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(com.ilimi.graph.dac.model
	 * .Node)
	 */
	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {

		Response response = new Response();
		LOGGER.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		LOGGER.log("Calling the 'Review' Initializer for Node Id: " + contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		LOGGER.log("Review Operation Finished Successfully for Node ID: " , contentId, "INFO");

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			LOGGER.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " + contentId, "INFO");
			response.put(ContentAPIParams.publishStatus.name(), "Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		}
		else {
			LOGGER.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " , node.getIdentifier(), "INFO");
			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#review(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response review(String contentId, Node node, boolean isAsync) {

		LOGGER.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.log("Calling the 'Review' Initializer for Node ID: " , contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}
}
