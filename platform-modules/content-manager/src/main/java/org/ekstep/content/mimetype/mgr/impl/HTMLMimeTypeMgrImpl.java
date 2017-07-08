package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class HTMLMimeTypeMgrImpl is a implementation of IMimeTypeManager for
 * Mime-Type as <code>application/vnd.ekstep.html-archive</code> or for HTML
 * Content.
 * 
 * @author Azhar
 * 
 * @see IMimeTypeManager
 * @see ECMLMimeTypeMgrImpl
 * @see APKMimeTypeMgrImpl
 * @see CollectionMimeTypeMgrImpl
 * @see AssetsMimeTypeMgrImpl
 */
public class HTMLMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/* Logger */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		LOGGER.log("Calling Upload Content For Node ID: " + node.getIdentifier(), "Uploaded File :" + uploadFile);
		return uploadContentArtifact(contentId, node, uploadFile);
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

		
		LOGGER.log("Calling the 'Review' Initializer for Node Id: " , contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		LOGGER.log("Review Operation Finished Successfully for Node ID: " , contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			LOGGER.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " , contentId);

			response.put(ContentAPIParams.publishStatus.name(),
					"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		} else {
			LOGGER.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " , contentId);
			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}

		return response;
	}
	
	@Override
	public Response review(String contentId, Node node, boolean isAsync) {

		LOGGER.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.log("Calling the 'Review' Initializer for Node ID: " , node.getIdentifier());
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
