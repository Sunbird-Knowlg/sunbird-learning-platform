package org.ekstep.sync.tool.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.sync.tool.util.CompositeIndexGenerateMessage;
import org.ekstep.sync.tool.util.CompositeIndexSyncMessage;
import org.ekstep.telemetry.logger.TelemetryManager;

public class CompositeIndexSyncManager{// extends BaseManager{

	private ControllerUtil util = new ControllerUtil();
	public void syncNode(String graphId, String identifier) throws Exception{
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		if (StringUtils.isBlank(identifier))
			throw new ClientException("BLANK_IDENTIFIER", "Identifier is blank.");
		TelemetryManager.log("Composite index sync : " + graphId + " | Identifier: " + identifier);
		Node node = util.getNode(graphId, identifier);//getNode(graphId, identifier);
		
		if (null != node) {
			Map<String, Object> kafkaMessage = CompositeIndexGenerateMessage.getKafkaMessage(node);
			System.out.println("kafkaMessage: " + kafkaMessage);
			CompositeIndexSyncMessage com = new CompositeIndexSyncMessage();
			com.processMessage(kafkaMessage);
			
		} else {
			throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND", "Object not found: " + identifier);
		}
	}
	
	/*private Node getNode(String graphId, String identifier) {
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), identifier);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ResourceNotFoundException("OBJECT_NOT_FOUND", "Object not found: " + identifier);
		else {
			Node node = (Node) listRes.get(GraphDACParams.node.name());
			return node;
		}
	}
	*/
	
	
	
}
