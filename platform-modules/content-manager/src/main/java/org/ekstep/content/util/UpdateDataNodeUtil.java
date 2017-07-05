package org.ekstep.content.util;

import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.service.common.DACConfigurationConstants;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;

/*
 * This is the only class which is using Base Manager From Top Level in hierarchy. 
 * Since it is Utility Class it can extends any class and not violating the solution structure.
 */
public class UpdateDataNodeUtil extends BaseManager {

	/** The logger. */
	private static ILogger LOGGER = new PlatformLogger(UpdateDataNodeUtil.class.getName());

	public Response updateDataNode(Node node) {
		LOGGER.log("Node: ", node);
		Response response = new Response();
		if (node != null) {
			LOGGER.log("Updating Data Node Id: " + node.getIdentifier());
			
			// Setting default version key for internal node update
			String graphPassportKey = Configuration.getProperty(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
			node.getMetadata().put(GraphDACParams.versionKey.name(), graphPassportKey);

			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.updateDataNode.name());
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			response = getResponse(updateReq, LOGGER);
		}

		LOGGER.log("Returning Response of 'updateDataNode' Call.");
		return response;
	}

}
