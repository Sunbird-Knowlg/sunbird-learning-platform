package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentOperations;
import com.ilimi.taxonomy.content.pipeline.initializer.InitializePipeline;
import com.ilimi.taxonomy.content.util.AsyncContentOperationUtil;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

// TODO: Auto-generated Javadoc
/**
 * The Class CollectionMimeTypeMgrImpl is a implementation of IMimeTypeManager
 * for Mime-Type as <code>application/vnd.ekstep.content-collection</code> or
 * for Collection Content.
 * 
 * @author Azhar
 * 
 * @see IMimeTypeManager
 * @see HTMLMimeTypeMgrImpl
 * @see APKMimeTypeMgrImpl
 * @see ECMLMimeTypeMgrImpl
 * @see AssetsMimeTypeMgrImpl
 */
@Component("CollectionMimeTypeMgrImpl")
public class CollectionMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/* Logger */
	private static Logger LOGGER = LogManager.getLogger(CollectionMimeTypeMgrImpl.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(Node node, File uploadFile, boolean isAsync) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadFile.getName());

		LOGGER.info("Calling Upload Content For Node ID: " + node.getIdentifier());
		return uploadContentArtifact(node, uploadFile);
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
		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.info("Calling the 'Publish' Initializer for Node Id: " + node.getIdentifier());
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		LOGGER.info("Review Operation Finished Successfully for Node ID: " + node.getIdentifier());

		LOGGER.debug("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);
		
		AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, parameterMap);
		LOGGER.info("Publish Operation Started Successfully in 'Async Mode' for Node Id: " + node.getIdentifier());

		response.put(ContentAPIParams.publishStatus.name(),
				"Publish Operation for Content Id '" + node.getIdentifier() + "' Started Successfully!");
		
		return response;
	}
	
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
