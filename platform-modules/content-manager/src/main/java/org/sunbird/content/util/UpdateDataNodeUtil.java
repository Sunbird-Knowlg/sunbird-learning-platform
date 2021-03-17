package org.sunbird.content.util;

import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.mgr.BaseManager;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.service.common.DACConfigurationConstants;
import org.sunbird.telemetry.logger.TelemetryManager;

/*
 * This is the only class which is using Base Manager From Top Level in hierarchy. 
 * Since it is Utility Class it can extends any class and not violating the solution structure.
 */
public class UpdateDataNodeUtil extends BaseManager {

	public Response updateDataNode(Node node) {
		Response response = new Response();
		if (node != null) {
			TelemetryManager.log("Updating Data Node Id: " + node.getIdentifier());
			
			// Setting default version key for internal node update
			String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
			node.getMetadata().put(GraphDACParams.versionKey.name(), graphPassportKey);

			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.updateDataNode.name());
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			response = getResponse(updateReq);
		}

		TelemetryManager.log("Returning Response of 'updateDataNode' Call.");
		return response;
	}

}
