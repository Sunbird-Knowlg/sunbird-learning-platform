package org.ekstep.compositesearch.mgr.impl;

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
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.dac.enums.GraphDACParams;
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
		List<DefinitionDTO> lstDefDTO = getDefinitionDTOList(result);
		List<Map<String, Object>> messages = genCompositeSearchMessages(graphId, lstDefDTO);
		Response response = pushMessageToKafka(messages);
		
		return response;
	}
	
	@Override
	public Response search(Request request) {
		SearchProcessor processor = new SearchProcessor();
		try {
			List<Object> lstResult = processor.processSearch(getSearchDTO(request));
			return getCompositeSearchResponse(lstResult);
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_UNKNOWN_ERROR.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SearchDTO getSearchDTO(Request request) throws Exception {
		SearchDTO searchObj = new SearchDTO();
		try {
			Map<String, Object> req = request.getRequest();
			String queryString = (String) req.get(CompositeSearchParams.query.name());
			int limit = 1000;
			
	/*		if (StringUtils.isBlank(queryString))
				throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_INVALID_QUERY_STRING.name(),
						"Query String is blank.");
	*/
			if (null != req.get(CompositeSearchParams.limit.name())) {
				limit = (int) req.get(CompositeSearchParams.limit.name());
			}
			List<Map> properties = new ArrayList<Map>();
			List<String> fields = (List<String>) req.get(CompositeSearchParams.fields.name());
			Map<String, Object> filters = (Map<String, Object>) req.get(CompositeSearchParams.filters.name());
			properties.addAll(getSearchQueryProperties(queryString, fields));
			properties.addAll(getSearchFilterProperties(filters));
			searchObj.setProperties(properties);
			searchObj.setLimit(limit);
			searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		} catch(ClassCastException e) {
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_INVALID_PARAMS.name(),
					"Invalid Input.");
		}
		return searchObj;
	}
	
	private List<Map<String, Object>> getSearchQueryProperties(String queryString, List<String> fields) {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		if(queryString != null && !queryString.isEmpty()){
			if (null == fields || fields.size() <= 0) {
				Map<String, Object> property = new HashMap<String, Object>();
				property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
				property.put(CompositeSearchParams.propertyName.name(), "*");
				property.put(CompositeSearchParams.values.name(), Arrays.asList(queryString));
				properties.add(property);
			} else {
				for (String field: fields) {
					Map<String, Object> property = new HashMap<String, Object>();
					property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
					property.put(CompositeSearchParams.propertyName.name(), field);
					property.put(CompositeSearchParams.values.name(), Arrays.asList(queryString));
					properties.add(property);
				}
			}
		}
		return properties;
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getSearchFilterProperties(Map<String, Object> filters) throws Exception {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		boolean objTypeFilter = false;
		boolean statusFilter = false;
		if (null != filters && !filters.isEmpty()) { 
			for (Entry<String, Object> entry: filters.entrySet()) {
				Object filterObject = entry.getValue();
				if(filterObject instanceof Map){
					Map<String, Object> filterMap = (Map<String, Object>) filterObject;
					for(Map.Entry<String, Object> filterEntry: filterMap.entrySet()){
						Map<String, Object> property = new HashMap<String, Object>();
						property.put(CompositeSearchParams.values.name(), filterEntry.getValue());
						property.put(CompositeSearchParams.propertyName.name(), entry.getKey());
						switch(filterEntry.getKey()){
							case "startsWith":{
								property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH);
								break;
							}
							case "endsWith":{
								property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH);
								break;
							}
							case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN:
							case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS:
							case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS:
							case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN:{
								property.put(CompositeSearchParams.operation.name(), filterEntry.getKey());
								break;
							}
							case "value":{
								property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
								break;
							}
							default:{
								throw new Exception("Unsupported operation");
							}
						}
						properties.add(property);
					}
				}
				else{
					Map<String, Object> property = new HashMap<String, Object>();
					property.put(CompositeSearchParams.values.name(), entry.getValue());
					property.put(CompositeSearchParams.propertyName.name(), entry.getKey());
					property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
					properties.add(property);
				}
				if (StringUtils.equals(GraphDACParams.objectType.name(), entry.getKey()))
				    objTypeFilter = true;
				if (StringUtils.equals(GraphDACParams.status.name(), entry.getKey()))
				    statusFilter = true;
			}
		}
		if (!objTypeFilter) {
		    Map<String, Object> property = new HashMap<String, Object>();
		    property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
            property.put(CompositeSearchParams.propertyName.name(), GraphDACParams.objectType.name());
            String[] objectTypes = new String[]{"Domain", "Dimension", "Concept", "Content", "Word", "Method", "Misconception", "AssessmentItem"};
            property.put(CompositeSearchParams.values.name(), Arrays.asList(objectTypes));
            properties.add(property);
		}
		if (!statusFilter) {
		    Map<String, Object> property = new HashMap<String, Object>();
            property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
            property.put(CompositeSearchParams.propertyName.name(), GraphDACParams.status.name());
            property.put(CompositeSearchParams.values.name(), Arrays.asList(new String[]{"Live"}));
            properties.add(property);
		}
		return properties;
	}
	
	@SuppressWarnings("unchecked")
    private Response getCompositeSearchResponse(List<Object> lstResult) {
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("Success");
		response.setParams(params);
		response.setResponseCode(ResponseCode.OK);
		if (null != lstResult && !lstResult.isEmpty()) {
		    Map<String, List<Map<String, Object>>> result = new HashMap<String, List<Map<String, Object>>>();
		    for (Object obj : lstResult) {
		        if (obj instanceof Map) {
		            Map<String, Object> map = (Map<String, Object>) obj;
		            String objectType = (String) map.get(GraphDACParams.objectType.name());
		            if (StringUtils.isNotBlank(objectType)) {
		                String key = getResultParamKey(objectType);
		                if (StringUtils.isNotBlank(key)) {
		                    List<Map<String, Object>> list = result.get(key);
		                    if (null == list) {
		                        list = new ArrayList<Map<String, Object>>();
		                        result.put(key, list);
		                        response.put(key, list);
		                    }
		                    list.add(map);
		                }
		            }
		        }
		    }
		}
		return response;
	}
	
	private String getResultParamKey(String objectType) {
	    if (StringUtils.isNotBlank(objectType)) {
	        if (StringUtils.equalsIgnoreCase("Domain", objectType))
	            return "domains";
	        else if (StringUtils.equalsIgnoreCase("Dimension", objectType))
                return "dimensions";
	        else if (StringUtils.equalsIgnoreCase("Concept", objectType))
                return "concepts";
	        else if (StringUtils.equalsIgnoreCase("Method", objectType))
                return "methods";
	        else if (StringUtils.equalsIgnoreCase("Misconception", objectType))
                return "misconceptions";
	        else if (StringUtils.equalsIgnoreCase("Content", objectType))
                return "content";
	        else if (StringUtils.equalsIgnoreCase("AssessmentItem", objectType))
                return "items";
	        else if (StringUtils.equalsIgnoreCase("ItemSet", objectType))
                return "itemsets";
	        else if (StringUtils.equalsIgnoreCase("Word", objectType))
                return "words";
	        else if (StringUtils.equalsIgnoreCase("Synset", objectType))
                return "synsets";
	        else
	            return "other";
	    }
	    return null;
	}
	
	
	private Response getAllDefinitions(String graphId) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		return getResponse(request, LOGGER);
	}
	
	@SuppressWarnings("unchecked")
	private List<DefinitionDTO> getDefinitionDTOList(Map<String, Object> result) {
		List<DefinitionDTO> lstDefDTO = new ArrayList<DefinitionDTO>();
		for (Entry<String, Object> def: result.entrySet()) {
			lstDefDTO.addAll((List<DefinitionDTO>) def.getValue());
		}
		
		return lstDefDTO;
	}
	
	private List<Map<String, Object>> genCompositeSearchMessages(String graphId, List<DefinitionDTO> lstDefDTO) {
		List<Map<String, Object>> lstMessages = new ArrayList<Map<String, Object>>();
		for(DefinitionDTO def: lstDefDTO) {
			lstMessages.add(getCompositeSearchMessage(graphId, def));
		}
		
		return lstMessages;
	}
	
	private Map<String, Object> getCompositeSearchMessage(String graphId, DefinitionDTO def) {
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
	
	private Response pushMessageToKafka(List<Map<String, Object>> messages) {
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
