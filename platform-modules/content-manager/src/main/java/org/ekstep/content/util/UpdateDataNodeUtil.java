package org.ekstep.content.util;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.service.common.DACConfigurationConstants;

import com.ilimi.common.mgr.BaseManager;

/*
 * This is the only class which is using Base Manager From Top Level in hierarchy. 
 * Since it is Utility Class it can extends any class and not violating the solution structure.
 */
public class UpdateDataNodeUtil extends BaseManager {

	public Response updateDataNode(Node node) {
		PlatformLogger.log("Node: ", node);
		Response response = new Response();
		if (node != null) {
			PlatformLogger.log("Updating Data Node Id: " + node.getIdentifier());
			
			// Setting default version key for internal node update
			String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
			node.getMetadata().put(GraphDACParams.versionKey.name(), graphPassportKey);

			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.updateDataNode.name());
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			response = getResponse(updateReq);
		}

		PlatformLogger.log("Returning Response of 'updateDataNode' Call.");
		return response;
	}

}
