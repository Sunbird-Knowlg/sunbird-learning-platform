package org.ekstep.compositesearch.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.mgr.BaseCompositeSearchManager;
import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.ekstep.searchindex.producer.KafkaMessageProducer;
import org.neo4j.graphdb.Node;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

@Component
public class CompositeSearchManagerImpl extends BaseCompositeSearchManager implements ICompositeSearchManager {
	
	private static Logger LOGGER = LogManager.getLogger(ICompositeSearchManager.class.getName());

	@Override
	public Response sync(String graphId, Request request) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		LOGGER.info("Get All Definitions : " + graphId);
		Map<String, Object> result = getAllDefinitions(graphId).getResult();
		List<DefinitionDTO> lstDefDTO = getDefDTOList(result);
		List<Map<String, Object>> messages = genCSearchMessages(graphId, lstDefDTO);
		Response response = pushMsgToKafka(messages);
		
		return response;
	}
	
	@Override
	public Response search(Request request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Response getAllDefinitions(String graphId) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		return getResponse(request, LOGGER);
	}
	
	@SuppressWarnings("unchecked")
	private List<DefinitionDTO> getDefDTOList(Map<String, Object> result) {
		List<DefinitionDTO> lstDefDTO = new ArrayList<DefinitionDTO>();
		for (Entry<String, Object> def: result.entrySet()) {
			lstDefDTO.addAll((List<DefinitionDTO>) def.getValue());
		}
		
		return lstDefDTO;
	}
	
	private List<Map<String, Object>> genCSearchMessages(String graphId, List<DefinitionDTO> lstDefDTO) {
		List<Map<String, Object>> lstMessages = new ArrayList<Map<String, Object>>();
		for(DefinitionDTO def: lstDefDTO) {
			lstMessages.add(getCSearchMessage(graphId, def));
		}
		
		return lstMessages;
	}
	
	private Map<String, Object> getCSearchMessage(String graphId, DefinitionDTO def) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> transactionData = new HashMap<String, Object>();
		transactionData.put(CompositeSearchParams.addedProperties.name(), new HashMap<String, Object>());
		transactionData.put(CompositeSearchParams.removedProperties.name(), new ArrayList<String>());
		transactionData.put(CompositeSearchParams.addedTags.name(), new ArrayList<String>());
		transactionData.put(CompositeSearchParams.removedTags.name(), new ArrayList<String>());
		map.put(CompositeSearchParams.operationType.name(), GraphDACParams.UPDATE.name());
		map.put(CompositeSearchParams.graphId.name(), graphId);
		map.put(CompositeSearchParams.nodeGraphId.name(), def.getIdentifier());
		map.put(CompositeSearchParams.nodeUniqueId.name(), def.getIdentifier());
		map.put(CompositeSearchParams.objectType.name(), def.getObjectType());
		map.put(CompositeSearchParams.nodeType.name(), CompositeSearchParams.DEFINITION_NODE.name());
		map.put(CompositeSearchParams.transactionData.name(), transactionData);
		
		return map;
	}
	
	private Response pushMsgToKafka(List<Map<String, Object>> messages) {
		Response response = new Response();
		if (messages.size() <= 0) {
			response.put(CompositeSearchParams.graphSyncStatus.name(), "No Graph Objects to Sync!");
			return response;
		}
		System.out.println("Sending to KAFKA.... ");
		KafkaMessageProducer producer = new KafkaMessageProducer();
		producer.init();
		for (Map<String, Object> message: messages) {
			System.out.println("Message : " + message);
			producer.pushMessage(message);
		}
		System.out.println("Sending to KAFKA : FINISHED");
		response.put(CompositeSearchParams.graphSyncStatus.name(), "Graph Sync Started Successfully!");
		
		return response;
	}

}
