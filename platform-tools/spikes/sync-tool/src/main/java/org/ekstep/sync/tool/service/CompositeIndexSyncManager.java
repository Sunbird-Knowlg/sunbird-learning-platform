package org.ekstep.sync.tool.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.CompositeSearchErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.sync.tool.util.CompositeIndexMessageGenerator;
import org.ekstep.sync.tool.util.CompositeIndexSyncer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompositeIndexSyncManager {

	private ControllerUtil util = new ControllerUtil();
	
	@Autowired
	private CompositeIndexSyncer compositeIndexSyncer;


	@PostConstruct
	public void init() throws Exception {
		compositeIndexSyncer.createCSIndexIfNotExist();
	}

	public void syncNode(String graphId, List<String> identifiers) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		if (identifiers.isEmpty())
			throw new ClientException("BLANK_IDENTIFIER", "Identifier is blank.");
		
		Set<String> ids = new HashSet<>(identifiers);
		if(ids.size()!=identifiers.size())
			System.out.println("unique number of node identifiers: "+ids.size());
		Response response = util.getDataNodes(graphId, identifiers);
		if (response.getResponseCode() != ResponseCode.OK)
			throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND",
					"Error: " + response.getParams().getErrmsg());
		List<Node> nodes = (List<Node>) response.getResult().get(GraphDACParams.node_list.name());
		if (nodes == null || nodes.isEmpty())
			throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND", "Objects not found ");
		for (Node node : nodes) {
			Map<String, Object> csMessage = CompositeIndexMessageGenerator.getMessage(node);
			compositeIndexSyncer.processMessage(csMessage);
			ids.remove(node.getIdentifier());
		}
		if(ids.size()!=0)
			System.out.println("("+ids.size()+") Nodes not found: "+ids +", remaining nodes got synced successfully");
	}

	public void syncNode(String graphId, String objectType) throws Exception {
		List<Node> nodes = getNodeByObjectType(graphId, objectType);
		if (!nodes.isEmpty()) {
			int i = 1;
			for (Node node : nodes) {
				if (i % 1000 == 0)
					System.out.println("IDENTIFIER GOING FOR PROCESS: " + i); // TODO: Remove this line

				if (null != node) {
					Map<String, Object> csMessage = CompositeIndexMessageGenerator.getMessage(node);
					compositeIndexSyncer.processMessage(csMessage);
				}
				i++;
			}
		}

	}

	private List<Node> getNodeByObjectType(String graphId, String objectType) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		DefinitionDTO def;
		List<Node> identifiers = new ArrayList<>();
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if (null != def) {
				int start = 0;
				int batch = 1000;
				boolean found = true;
				while (found) {
					List<Node> nodes = util.getNodes(graphId, def.getObjectType(), start, batch);
					if (null != nodes && !nodes.isEmpty()) {
						for (Node node : nodes) {
							identifiers.add(node);
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
