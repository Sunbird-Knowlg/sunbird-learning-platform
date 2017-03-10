package com.ilimi.taxonomy.content.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

/*
 * This is the only class which is using Base Manager From Top Level in hierarchy. 
 * Since it is Utility Class it can extends any class and not violating the solution structure.
 */
public class UpdateDataNodeUtil extends BaseManager {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(UpdateDataNodeUtil.class.getName());

	public Response updateDataNode(Node node) {
		LOGGER.debug("Node: ", node);
		Response response = new Response();
		if (node != null) {
			LOGGER.debug("Updating Data Node Id: " + node.getIdentifier());
			
			// Setting default version key for internal node update
			String graphPassportKey = Configuration.getProperty(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
			node.getMetadata().put(GraphDACParams.versionKey.name(), graphPassportKey);

			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.updateDataNode.name());
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			response = getResponse(updateReq, LOGGER);
		}

		LOGGER.debug("Returning Response of 'updateDataNode' Call.");
		return response;
	}

}
