package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	/* Logger */
	private static Logger LOGGER = LogManager.getLogger(YoutubeMimeTypeManager.class.getName());

	/* The youtubeUrl regex */
	private static final String YOUTUBE_REGEX = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(Node node, File uploadFile, boolean isAsync) {
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
	public Response publish(Node node, boolean isAsync) {

		LOGGER.debug("Node: ", node);
		Response response = new Response();
		if(null == node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name())){
			throw new ClientException(ContentErrorCodes.MISSING_YOUTUBE_URL.name(), ContentErrorMessageConstants.MISSING_YOUTUBE_URL, " | [Invalid or 'missing' youtube Url.] Publish Operation Failed");
		}
		Boolean isValidYouTubeUrl = Pattern.matches(YOUTUBE_REGEX, node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()).toString());
		LOGGER.info("Validating if the given youtube url is valid or not" + isValidYouTubeUrl);
		if (!isValidYouTubeUrl) {
			throw new ClientException(ContentErrorCodes.INVALID_YOUTUBE_URL.name(), ContentErrorMessageConstants.INVALID_YOUTUBE_URL, " | [Invalid or 'null' operation.] Publish Operation Failed");
		}
		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.debug("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		LOGGER.info("Calling the 'Review' Initializer for Node Id: " + node.getIdentifier());
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		LOGGER.info("Review Operation Finished Successfully for Node ID: " + node.getIdentifier());

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, parameterMap);
			LOGGER.info("Publish Operation Started Successfully in 'Async Mode' for Node Id: " + node.getIdentifier());
			response.put(ContentAPIParams.publishStatus.name(), "Publish Operation for Content Id '" + node.getIdentifier() + "' Started Successfully!");
		}
		else {
			LOGGER.info("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " + node.getIdentifier());
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
	public Response review(Node node, boolean isAsync) {
		LOGGER.debug("Node: ", node);

		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.info("Calling the 'Review' Initializer for Node ID: " + node.getIdentifier());
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}
}
