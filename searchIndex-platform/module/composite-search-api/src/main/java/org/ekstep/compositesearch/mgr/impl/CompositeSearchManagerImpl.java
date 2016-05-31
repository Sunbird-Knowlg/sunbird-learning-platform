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
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

@Component
public class CompositeSearchManagerImpl extends BaseCompositeSearchManager implements ICompositeSearchManager {
	
	private static Logger LOGGER = LogManager.getLogger(ICompositeSearchManager.class.getName());
	private static final int SYNC_BATCH_SIZE = 1000;
	
	@Override
	public Response sync(String graphId, String objectType, Request request) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		LOGGER.info("Get All Definitions : " + graphId);
		List<Map<String, Object>> messages = null;
		if (StringUtils.isNotBlank(objectType)) {
		    Map<String, Object> result = getDefinition(graphId, objectType).getResult();
		    DefinitionDTO dto = (DefinitionDTO) result.get(GraphDACParams.definition_node.name());
		    messages = genCompositeSearchMessage(graphId, dto);
		} else {
		    Map<String, Object> result = getAllDefinitions(graphId).getResult();
	        List<DefinitionDTO> lstDefDTO = getDefinitionDTOList(result);
	        messages = genCompositeSearchMessages(graphId, lstDefDTO);
		}
		Response response = OK();
		if (null != messages && !messages.isEmpty())
		    response = pushMessageToKafka(messages);
		return response;
	}
	
	@Override
	public Response search(Request request) {
		SearchProcessor processor = new SearchProcessor();
		try {
			Map<String,Object> lstResult = processor.processSearch(getSearchDTO(request), true);
			return getCompositeSearchResponse(lstResult);
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_UNKNOWN_ERROR.name(), "Search Failed", ResponseCode.SERVER_ERROR);
		}
	}
	
	@Override
    public Response metrics(Request request) {
        SearchProcessor processor = new SearchProcessor();
        try {
            Map<String,Object> lstResult = processor.processSearch(getSearchDTO(request), false);
            return getCompositeSearchResponse(lstResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_UNKNOWN_ERROR.name(), "Search Failed", ResponseCode.SERVER_ERROR);
        }
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SearchDTO getSearchDTO(Request request) throws Exception {
		SearchDTO searchObj = new SearchDTO();
		try {
			Map<String, Object> req = request.getRequest();
			String queryString = (String) req.get(CompositeSearchParams.query.name());
			int limit = 100;
			
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
			List<String> exists = (List<String>) req.get(CompositeSearchParams.exists.name());
			List<String> notExists = (List<String>) req.get(CompositeSearchParams.not_exists.name());
			List<String> facets = getList(req.get(CompositeSearchParams.facets.name()));
			Map<String, String> sortBy = (Map<String, String>) req.get(CompositeSearchParams.sort_by.name());
			properties.addAll(getAdditionalFilterProperties(exists, CompositeSearchParams.exists.name()));
			properties.addAll(getAdditionalFilterProperties(notExists, CompositeSearchParams.not_exists.name()));
			properties.addAll(getSearchQueryProperties(queryString, fields));
			properties.addAll(getSearchFilterProperties(filters));
			searchObj.setSortBy(sortBy);
			searchObj.setFacets(facets);
			searchObj.setProperties(properties);
			searchObj.setLimit(limit);
			searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		} catch(ClassCastException e) {
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_INVALID_PARAMS.name(),
					"Invalid Input.");
		}
		return searchObj;
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getList(Object param){
		List<String> paramList;
		try{
			paramList = (List<String>) param;
		}
		catch(Exception e){
			String str = (String) param;
			paramList = Arrays.asList(str);
		}
		return paramList;
	}
	
	private List<Map<String, Object>> getAdditionalFilterProperties(List<String> fieldList, String operation) {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		if(fieldList != null){
			for (String field : fieldList) {
				String searchOperation = "";
				switch (operation) {
				case "exists": {
					searchOperation = CompositeSearchConstants.SEARCH_OPERATION_EXISTS;
					break;
				}
				case "not_exists": {
					searchOperation = CompositeSearchConstants.SEARCH_OPERATION_NOT_EXISTS;
					break;
				}
				}
				Map<String, Object> property = new HashMap<String, Object>();
				property.put(CompositeSearchParams.operation.name(), searchOperation);
				property.put(CompositeSearchParams.propertyName.name(), field);
				property.put(CompositeSearchParams.values.name(), Arrays.asList(field));
				properties.add(property);
			}
		}
		return properties;
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
	private Response getCompositeSearchResponse(Map<String, Object> searchResponse) {
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("Success");
		response.setParams(params);
		response.setResponseCode(ResponseCode.OK);

		for (Map.Entry<String, Object> entry : searchResponse.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("results")) {
				List<Object> lstResult = (List<Object>) entry.getValue();
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
			} else {
				response.put(entry.getKey(), entry.getValue());
			}
		}
		return response;
	}
	
    private Response getCompositeSearchCountResponse(Map<String, Object> countResponse) {
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("Success");
		response.setParams(params);
		response.setResponseCode(ResponseCode.OK);
		
		if (null != countResponse.get("count")){
			response.put("count", (Double) countResponse.get("count"));
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
	
	private Response getDefinition(String graphId, String objectType) {
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition");
        request.put(GraphDACParams.object_type.name(), objectType);
        return getResponse(request, LOGGER);
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
			lstMessages.addAll(genCompositeSearchMessage(graphId, def));
		}
		return lstMessages;
	}
	
    private List<Map<String, Object>> genCompositeSearchMessage(String graphId, DefinitionDTO def) {
	    List<Map<String, Object>> lstMessages = new ArrayList<Map<String, Object>>();
	    int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getNodes(graphId, def.getObjectType(), startPosistion, SYNC_BATCH_SIZE);
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    lstMessages.add(getKafkaMessage(node));
                }
                startPosistion += SYNC_BATCH_SIZE;
                System.out.println("Fetched " + startPosistion + " " + def.getObjectType() + " objects");
            } else {
                found = false;
                break;
            }
        }
	    return lstMessages;
    }
	
	private Map<String, Object> getKafkaMessage(Node node) {
	    Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> transactionData = new HashMap<String, Object>();
        if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
                Map<String, Object> valueMap=new HashMap<String, Object>();
                valueMap.put("ov", null); // old value
                valueMap.put("nv", entry.getValue()); // new value
                propertyMap.put((String) entry.getKey(), valueMap);
            }
            transactionData.put(CompositeSearchParams.properties.name(), propertyMap);
        } else
            transactionData.put(CompositeSearchParams.properties.name(), new HashMap<String, Object>());
        transactionData.put(CompositeSearchParams.addedTags.name(), null == node.getTags() ? new ArrayList<String>() : node.getTags());
        transactionData.put(CompositeSearchParams.removedTags.name(), new ArrayList<String>());
        List<Map<String, Object>> relations = new ArrayList<Map<String, Object>>();
        if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
            for (Relation rel : node.getInRelations()) {
                Map<String, Object> relMap = new HashMap<>();
                relMap.put("rel", rel.getRelationType());
                relMap.put("id", rel.getStartNodeId());
                relMap.put("dir", "IN");
                relMap.put("type", rel.getStartNodeObjectType());
                relMap.put("label", getLabel(rel.getStartNodeMetadata()));
                relations.add(relMap);
            }
        }
        if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
            for (Relation rel : node.getOutRelations()) {
                Map<String, Object> relMap = new HashMap<>();
                relMap.put("rel", rel.getRelationType());
                relMap.put("id", rel.getEndNodeId());
                relMap.put("dir", "OUT");
                relMap.put("type", rel.getEndNodeObjectType());
                relMap.put("label", getLabel(rel.getEndNodeMetadata()));
                relations.add(relMap);
            }
        }
        transactionData.put(CompositeSearchParams.addedRelations.name(), relations);
        map.put(CompositeSearchParams.operationType.name(), GraphDACParams.UPDATE.name());
        map.put(CompositeSearchParams.graphId.name(), node.getGraphId());
        map.put(CompositeSearchParams.nodeGraphId.name(), node.getId());
        map.put(CompositeSearchParams.nodeUniqueId.name(), node.getIdentifier());
        map.put(CompositeSearchParams.objectType.name(), node.getObjectType());
        map.put(CompositeSearchParams.nodeType.name(), SystemNodeTypes.DATA_NODE.name());
        map.put(CompositeSearchParams.transactionData.name(), transactionData);
        map.put(CompositeSearchParams.syncMessage.name(), true);
        return map;
	}
	
	private String getLabel(Map<String, Object> metadata){
        if (null != metadata && !metadata.isEmpty()) {
            if (StringUtils.isNotBlank((String) metadata.get("name")))
                return (String) metadata.get("name");
            else if (StringUtils.isNotBlank((String) metadata.get("lemma")))
                return (String) metadata.get("lemma");
            else if (StringUtils.isNotBlank((String) metadata.get("title")))
                return (String) metadata.get("title");
            else if (StringUtils.isNotBlank((String) metadata.get("gloss")))
                return (String) metadata.get("gloss");
        }
        return "";
    }
	
	private Response pushMessageToKafka(List<Map<String, Object>> messages) {
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		if (null == messages || messages.size() <= 0) {
			response.put(CompositeSearchParams.graphSyncStatus.name(), "No Graph Objects to Sync!");
			response.setResponseCode(ResponseCode.CLIENT_ERROR);
			params.setStatus(CompositeSearchParams.success.name());
			response.setParams(params);
			return response;
		}
		System.out.println("Sending to KAFKA.... ");
		KafkaMessageProducer producer = new KafkaMessageProducer();
		producer.init();
		int index = 0;
		for (Map<String, Object> message: messages) {
			producer.pushMessage(message);
			index += 1;
			if (index != 0 && index%1000 == 0) {
			    try {
			        System.out.println("Sleeping for 2 seconds after pushing " + index + " messages");
			        Thread.sleep(2000);
			    } catch (Exception e) {
			    }
			}
		}
		response.put(CompositeSearchParams.graphSyncStatus.name(), "Graph Sync Started Successfully!");
		response.setResponseCode(ResponseCode.OK);
		response.setParams(params);
		params.setStatus(CompositeSearchParams.success.name());
		response.setParams(params);
		return response;
	}

	@Override
	public Response count(Request request) {
		SearchProcessor processor = new SearchProcessor();
		try {
			Map<String,Object> countResult = processor.processCount(getSearchDTO(request));
			return getCompositeSearchCountResponse(countResult);
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_UNKNOWN_ERROR.name(), "Search Failed", ResponseCode.SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
    private List<Node> getNodes(String graphId, String objectType, int startPosition, int batchSize) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.setResultSize(batchSize);
        sc.setStartPosition(startPosition);
        Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        req.put(GraphDACParams.get_tags.name(), true);
        Response listRes = getResponse(req, LOGGER);
        if (checkError(listRes))
            throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found for language: " + graphId);
        else {
            List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
            return nodes;
        }
    }

}
