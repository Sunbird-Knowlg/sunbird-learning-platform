package org.ekstep.graph.service.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Request;

public class Neo4JBoltNodeOperations {
	
	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltNodeOperations.class.getName());
	
	public com.ilimi.graph.dac.model.Node upsertNode(String graphId, com.ilimi.graph.dac.model.Node node,
			Request request) {
		LOGGER.debug("Graph Id: " + graphId);
		LOGGER.debug("Graph Engine Node: " + node);
		LOGGER.debug("Request: " + request);
		
		return node;
	}

}
