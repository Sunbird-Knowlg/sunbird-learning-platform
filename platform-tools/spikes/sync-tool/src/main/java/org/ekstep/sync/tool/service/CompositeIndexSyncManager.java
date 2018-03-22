package org.ekstep.sync.tool.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.CompositeSearchErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.sync.tool.util.CompositeIndexGenerateMessage;
import org.ekstep.sync.tool.util.CompositeIndexSyncMessage;
import org.ekstep.telemetry.logger.TelemetryManager;

public class CompositeIndexSyncManager{

	private ControllerUtil util = new ControllerUtil();
	
	public void syncNode(String graphId, List<String> identifiers) throws Exception{
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		if (identifiers.isEmpty())
			throw new ClientException("BLANK_IDENTIFIER", "Identifier is blank.");
		
		CompositeIndexSyncMessage com = new CompositeIndexSyncMessage();
		int i=1;
		for(String identifier: identifiers) {
			TelemetryManager.log("Composite index sync : " + graphId + " | Identifier: " + identifier);
			if(i%1000==0)System.out.println("IDENTIFIER GOING FOR PROCESS: " + i); 								//TODO: Remove this line
			Node node = util.getNode(graphId, identifier);
		
			if (null != node) {
				Map<String, Object> kafkaMessage = CompositeIndexGenerateMessage.getKafkaMessage(node);
				com.processMessage(kafkaMessage);
			} else {
				throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND", "Object not found: " + identifier);
			}
			i++;
		}
	}
	
	public void syncNode(String graphId, String objectType) throws Exception{
		List<Node> nodes = getNodeByObjectType(graphId, objectType);
		CompositeIndexSyncMessage compositeIndexSyncMessage = new CompositeIndexSyncMessage();
		if(!nodes.isEmpty()) {
			int i=1;
			for(Node node : nodes) {
				TelemetryManager.log("Composite index sync : " + graphId + " | Identifier: " + node.getIdentifier());
				if(i%1000==0)System.out.println("IDENTIFIER GOING FOR PROCESS: " + i); 								//TODO: Remove this line
				
			
				if (null != node) {
					Map<String, Object> kafkaMessage = CompositeIndexGenerateMessage.getKafkaMessage(node);
					compositeIndexSyncMessage.processMessage(kafkaMessage);
				}
				i++;
			}
		}
		
		
	}
	
	private List<Node> getNodeByObjectType(String graphId, String objectType) throws Exception{
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(), "Graph Id is blank.");
		DefinitionDTO def;
		List<Node> identifiers = new ArrayList<>();
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if(null != def) {
				int start = 0;
				int batch = 1000;
				boolean found = true;
				while (found) {
					List<Node> nodes = util.getNodes(graphId, def.getObjectType(), start, batch);
					if (null != nodes && !nodes.isEmpty()) {
						for (Node node : nodes) {
							identifiers.add(node);
							//System.out.println("***" + node.getIdentifier() + "***" + node.getMetadata().get("name"));
						}
						System.out.println("sent " + start + " + " + batch + " -- " + def.getObjectType() + " objects");
						start += batch;
					} else {
						found = false;
						break;
					}
				}
			}
		}
		return identifiers;
	}
	
	
	
	
}
