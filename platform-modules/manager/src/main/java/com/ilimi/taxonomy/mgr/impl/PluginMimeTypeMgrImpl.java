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
import com.ilimi.taxonomy.content.pipeline.initializer.InitializePipeline;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

/**
 * The Class PluginMimeTypeMgrImpl.
 */
@Component("PluginMimeTypeMgrImpl")
public class PluginMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager{
	
	private static Logger LOGGER = LogManager.getLogger(PluginMimeTypeMgrImpl.class.getName());

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.Node, java.io.File)
	 */
	@Override
	public Response upload(Node node, File uploadFile) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadFile.getName());

		LOGGER.info("Calling Upload Content For Node ID: " + node.getIdentifier());
		return uploadContentArtifact(node, uploadFile);
	}

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(com.ilimi.graph.dac.model.Node)
	 */
	@Override
	public Response publish(Node node) {
		LOGGER.debug("Node: ", node);

		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.info("Calling the 'Publish' Initializer for Node ID: " + node.getIdentifier());
		return pipeline.init(ContentAPIParams.publish.name(), parameterMap);
	}

}
