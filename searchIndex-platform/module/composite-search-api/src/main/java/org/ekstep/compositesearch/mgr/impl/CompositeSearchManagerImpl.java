package org.ekstep.compositesearch.mgr.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.producer.KafkaMessageProducer;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.neo4j.graphdb.Node;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
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
		SearchProcessor processor = new SearchProcessor();
		try {
			List<Object> lstResult = processor.processSearch(getSearchDTO(request));
			return getCSearchResponse(lstResult);
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_UNKNOWN_ERROR.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SearchDTO getSearchDTO(Request request) {
		Map<String, Object> req = request.getRequest();
		String queryString = (String) req.get(CompositeSearchParams.query.name());
		int limit = (int) req.get(CompositeSearchParams.limit.name());
		if (StringUtils.isBlank(queryString))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_INVALID_QUERY_STRING.name(),
					"Query String is blank.");
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		List<String> fields = (List<String>) req.get(CompositeSearchParams.fields.name());
		List<Map<String, Object>> filters = (List<Map<String, Object>>) req.get(CompositeSearchParams.filters.name());
		properties.addAll(getSearchQueryProp(queryString, fields));
		properties.addAll(getSearchFilterProp(filters));
		searchObj.setProperties(properties);
		searchObj.setLimit(limit);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		return searchObj;
	}
	
	private List<Map<String, Object>> getSearchQueryProp(String queryString, List<String> fields) {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		if (null == fields || fields.size() <= 0) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
			property.put(CompositeSearchParams.propertyName.name(), "*");
			property.put(CompositeSearchParams.values.name(), Arrays.asList(queryString));
			properties.add(property);
		}
		for (String field: fields) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
			property.put(CompositeSearchParams.propertyName.name(), field);
			property.put(CompositeSearchParams.values.name(), Arrays.asList(queryString));
			properties.add(property);
		}
		
		return properties;
	}
	
	private List<Map<String, Object>> getSearchFilterProp(List<Map<String, Object>> filters) {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> filter: filters) {
			for (Entry<String, Object> entry: filter.entrySet()) {
				Map<String, Object> property = new HashMap<String, Object>();
				property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
				property.put(CompositeSearchParams.propertyName.name(), entry.getKey());
				property.put(CompositeSearchParams.values.name(), entry.getValue());
				properties.add(property);
			}
		}
		
		return properties;
	}
	
	private Response getCSearchResponse(List<Object> lstResult) {
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("Success");
		response.setParams(params);
		response.setResponseCode(ResponseCode.OK);
		response.put("search_result", lstResult);
		
		return response;
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
		ResponseParams params = new ResponseParams();
		if (messages.size() <= 0) {
			response.put(CompositeSearchParams.graphSyncStatus.name(), "No Graph Objects to Sync!");
			response.setResponseCode(ResponseCode.CLIENT_ERROR);
			params.setStatus(CompositeSearchParams.success.name());
			response.setParams(params);
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
		response.setResponseCode(ResponseCode.OK);
		response.setParams(params);
		params.setStatus(CompositeSearchParams.success.name());
		response.setParams(params);
		return response;
	}

}
