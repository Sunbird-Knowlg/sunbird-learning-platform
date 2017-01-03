package org.ekstep.search.actor;

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
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

import com.ilimi.common.dto.CoverageIgnore;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.dac.enums.GraphDACParams;

import akka.actor.ActorRef;

public class SearchManager extends SearchBaseActor {

	private static Logger LOGGER = LogManager.getLogger(SearchManager.class.getName());

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
		String operation = request.getOperation();
		SearchProcessor processor = new SearchProcessor();
		try {
			if (StringUtils.equalsIgnoreCase(SearchOperations.INDEX_SEARCH.name(), operation)) {
				SearchDTO searchDTO = getSearchDTO(request);
				Map<String, Object> lstResult = processor.processSearch(searchDTO, true);
				OK(lstResult, parent);
			} else if (StringUtils.equalsIgnoreCase(SearchOperations.COUNT.name(), operation)) {
				Map<String, Object> countResult = processor.processCount(getSearchDTO(request));
				if (null != countResult.get("count")) {
					Double count = (Double) countResult.get("count");
					OK("count", count, parent);
				} else {
					ERROR("", "count is empty or null", ResponseCode.SERVER_ERROR, "", null, parent);
				}

			} else if (StringUtils.equalsIgnoreCase(SearchOperations.METRICS.name(), operation)) {
				Map<String, Object> lstResult = processor.processSearch(getSearchDTO(request), false);
				OK(getCompositeSearchResponse(lstResult), parent);

			} else if (StringUtils.equalsIgnoreCase(SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name(), operation)) {
				Map<String, Object> searchResponse = (Map<String, Object>) request.get("searchResult");
				OK(getCompositeSearchResponse(searchResponse), parent);

			}else if (StringUtils.equalsIgnoreCase(SearchOperations.MULTI_LANGUAGE_WORD_SEARCH.name(), operation)) {
				List<String> synsetIdList = (List<String>) request.get("synset_id_list");
				Map<String, Object> lstResult = processor.multiWordDocSearch(synsetIdList);
				OK(lstResult, parent);
			}else if (StringUtils.equalsIgnoreCase(SearchOperations.MULTI_LANGUAGE_SYNSET_SEARCH.name(), operation)) {
				List<String> synsetIdList = (List<String>) request.get("synset_ids");
				Map<String, Object> lstResult = processor.multiSynsetDocSearch(synsetIdList);
				OK(lstResult, parent);
			}
			else {
				LOGGER.info("Unsupported operation: " + operation);
				throw new ClientException(CompositeSearchErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error in SearchManager actor", e);
			handleException(e, getSender());
		} finally {
			if (null != processor)
				processor.destroy();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SearchDTO getSearchDTO(Request request) throws Exception {
		SearchDTO searchObj = new SearchDTO();
		try {
			Map<String, Object> req = request.getRequest();
			LOGGER.info("Search Request: " + req);
			String queryString = (String) req.get(CompositeSearchParams.query.name());
			int limit = 100;
			if (null != req.get(CompositeSearchParams.limit.name())) {
				limit = (int) req.get(CompositeSearchParams.limit.name());
			}
			LOGGER.info("Using limit: " + limit);
			Boolean fuzzySearch = (Boolean) request.get("fuzzy");
			if (null == fuzzySearch)
				fuzzySearch = false;
			LOGGER.info("Fuzzy Search: " + fuzzySearch);
			Boolean wordChainsRequest = (Boolean) request.get("traversal");
			if (null == wordChainsRequest)
				wordChainsRequest = false;
			LOGGER.info("Word chain request: " + wordChainsRequest);
			List<Map> properties = new ArrayList<Map>();
			Map<String, Object> filters = (Map<String, Object>) req.get(CompositeSearchParams.filters.name());
			LOGGER.info("Filters: " + filters);
			if(fuzzySearch && filters != null){
				Map<String, Double> weightagesMap = new HashMap<String, Double>();
				weightagesMap.put("default_weightage", 1.0);
				Object objectTypeFromFilter = filters.get(CompositeSearchParams.objectType.name());
				String objectType = null;
				if(objectTypeFromFilter != null){
					if(objectTypeFromFilter instanceof List){
						List objectTypeList = (List) objectTypeFromFilter;
						if (objectTypeList.size() > 0)
							objectType = (String) objectTypeList.get(0);
					} else if (objectTypeFromFilter instanceof String){
						objectType = (String) objectTypeFromFilter;
					}
				}
				LOGGER.info("Object Type: " + objectType);
				
				Object graphIdFromFilter = filters.get(CompositeSearchParams.graph_id.name());
				String graphId = null;
				if(graphIdFromFilter != null){
					if(graphIdFromFilter instanceof List){
						List graphIdList = (List) graphIdFromFilter;
						if (graphIdList.size() > 0)
							graphId = (String) graphIdList.get(0);
					} else if (graphIdFromFilter instanceof String){
						graphId = (String) graphIdFromFilter;
					}
				}
				LOGGER.info("Graph Id: " + graphId);
				
				if(StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(graphId)){
					Map<String, Object> objDefinition = ObjectDefinitionCache.getMetaData(objectType, graphId);
					//DefinitionDTO objDefinition = DefinitionCache.getDefinitionNode(graphId, objectType);
					String weightagesString = (String) objDefinition.get("weightages");
					if(StringUtils.isNotBlank(weightagesString)){
						weightagesMap = getWeightagesMap(weightagesString);
					}
				}
				LOGGER.info("Weightages: " + weightagesMap);
				searchObj.addAdditionalProperty("weightagesMap", weightagesMap);
			}
			
			List<String> exists = null;
			Object existsObject = req.get(CompositeSearchParams.exists.name());
			if(existsObject instanceof List){
				exists = (List<String>) existsObject; 
			} else if (existsObject instanceof String){
				exists =  new ArrayList<String>();
				exists.add((String) existsObject);
			}
			LOGGER.info("Exists: " + exists);
			
			List<String> notExists = null;
			Object notExistsObject = req.get(CompositeSearchParams.not_exists.name());
			if(notExistsObject instanceof List){
				notExists = (List<String>) notExistsObject; 
			} else if (notExistsObject instanceof String){
				notExists =  new ArrayList<String>();
				notExists.add((String) notExistsObject);
			}
			LOGGER.info("Not Exists: " + notExists);
			
			List<String> fieldsSearch = getList(req.get(CompositeSearchParams.fields.name()));
			LOGGER.info("Fields: " + fieldsSearch);
			List<String> facets = getList(req.get(CompositeSearchParams.facets.name()));
			LOGGER.info("Facets: " + facets);
			Map<String, String> sortBy = (Map<String, String>) req.get(CompositeSearchParams.sort_by.name());
			LOGGER.info("Sort By: " + sortBy);
			properties.addAll(getAdditionalFilterProperties(exists, CompositeSearchParams.exists.name()));
			properties.addAll(getAdditionalFilterProperties(notExists, CompositeSearchParams.not_exists.name()));
			//Changing fields to null so that search all fields but returns only the fields specified
			properties.addAll(getSearchQueryProperties(queryString, null)); 
			properties.addAll(getSearchFilterProperties(filters, wordChainsRequest));
			searchObj.setSortBy(sortBy);
			searchObj.setFacets(facets);
			searchObj.setProperties(properties);
			searchObj.setLimit(limit);
			searchObj.setFields(fieldsSearch);
			searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
			
			if (null != req.get(CompositeSearchParams.offset.name())) {
				int offset = (Integer) req.get(CompositeSearchParams.offset.name());
				LOGGER.info("Offset: " + offset);
				searchObj.setOffset(offset);
			}
			
			if (fuzzySearch != null) {
				searchObj.setFuzzySearch(fuzzySearch);
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_INVALID_PARAMS.name(),
					"Invalid Input.", e);
		}
		return searchObj;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Double> getWeightagesMap(String weightagesString) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Double> weightagesMap = new HashMap<String, Double>();
		ObjectMapper mapper = new ObjectMapper();
		if (weightagesString != null && !weightagesString.isEmpty()) {
			Map<String, Object> weightagesRequestMap = mapper.readValue(weightagesString,
					new TypeReference<Map<String, Object>>() {
					});

			for (Map.Entry<String, Object> entry : weightagesRequestMap.entrySet()) {
				Double weightage = Double.parseDouble(entry.getKey());
				if (entry.getValue() instanceof List) {
					List<String> fields = (List<String>) entry.getValue();
					for (String field : fields) {
						weightagesMap.put(field, weightage);
					}
				} else {
					String field = (String) entry.getValue();
					weightagesMap.put(field, weightage);
				}
			}
		}
		return weightagesMap;
	}

	private List<Map<String, Object>> getAdditionalFilterProperties(List<String> fieldList, String operation) {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		if (fieldList != null) {
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

	private List<String> getList(Object param) {
		List<String> paramList;
		try {
			paramList = (List<String>) param;
		} catch (Exception e) {
			String str = (String) param;
			paramList = Arrays.asList(str);
		}
		return paramList;
	}

	private List<Map<String, Object>> getSearchQueryProperties(String queryString, List<String> fields) {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		if (queryString != null && !queryString.isEmpty()) {
			if (null == fields || fields.size() <= 0) {
				Map<String, Object> property = new HashMap<String, Object>();
				property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
				property.put(CompositeSearchParams.propertyName.name(), "*");
				property.put(CompositeSearchParams.values.name(), Arrays.asList(queryString));
				properties.add(property);
			} else {
				for (String field : fields) {
					Map<String, Object> property = new HashMap<String, Object>();
					property.put(CompositeSearchParams.operation.name(),
							CompositeSearchConstants.SEARCH_OPERATION_LIKE);
					property.put(CompositeSearchParams.propertyName.name(), field);
					property.put(CompositeSearchParams.values.name(), Arrays.asList(queryString));
					properties.add(property);
				}
			}
		}
		return properties;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map<String, Object>> getSearchFilterProperties(Map<String, Object> filters, Boolean traversal)
			throws Exception {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		boolean statusFilter = false;
		if (null != filters && !filters.isEmpty()) {
			for (Entry<String, Object> entry : filters.entrySet()) {
				Object filterObject = entry.getValue();
				if (filterObject instanceof Map) {
					Map<String, Object> filterMap = (Map<String, Object>) filterObject;
					if (!filterMap.containsKey(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MIN)
							&& !filterMap.containsKey(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MAX)) {
						for (Map.Entry<String, Object> filterEntry : filterMap.entrySet()) {
							Map<String, Object> property = new HashMap<String, Object>();
							property.put(CompositeSearchParams.values.name(), filterEntry.getValue());
							property.put(CompositeSearchParams.propertyName.name(), entry.getKey());
							switch (filterEntry.getKey()) {
							case "startsWith": {
								property.put(CompositeSearchParams.operation.name(),
										CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH);
								break;
							}
							case "endsWith": {
								property.put(CompositeSearchParams.operation.name(),
										CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH);
								break;
							}
							case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN:
							case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS:
							case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS:
							case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN: {
								property.put(CompositeSearchParams.operation.name(), filterEntry.getKey());
								break;
							}
							case "value": {
								property.put(CompositeSearchParams.operation.name(),
										CompositeSearchConstants.SEARCH_OPERATION_LIKE);
								break;
							}
							default: {
								throw new Exception("Unsupported operation");
							}
							}
							properties.add(property);
						}
					} else {
						Map<String, Object> property = new HashMap<String, Object>();
						Map<String, Object> rangeMap = new HashMap<String, Object>();
						Object minFilterValue = filterMap.get(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MIN);
						if (minFilterValue != null) {
							rangeMap.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE, minFilterValue);
						}
						Object maxFilterValue = filterMap.get(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MAX);
						if (maxFilterValue != null) {
							rangeMap.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE, maxFilterValue);
						}
						property.put(CompositeSearchParams.values.name(), rangeMap);
						property.put(CompositeSearchParams.propertyName.name(), entry.getKey());
						property.put(CompositeSearchParams.operation.name(),
								CompositeSearchConstants.SEARCH_OPERATION_RANGE);
						properties.add(property);
					}
				} else {
					boolean emptyVal = false;
					if (null == filterObject) {
						emptyVal = true;
					} else if (filterObject instanceof List) {
						if (((List) filterObject).size() <= 0)
							emptyVal = true;
					} else if (filterObject instanceof Object[]) {
						if (((Object[]) filterObject).length <= 0)
							emptyVal = true;
					}
					if (!emptyVal) {
						Map<String, Object> property = new HashMap<String, Object>();
						property.put(CompositeSearchParams.values.name(), entry.getValue());
						property.put(CompositeSearchParams.propertyName.name(), entry.getKey());
						property.put(CompositeSearchParams.operation.name(),
								CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
						properties.add(property);
					}
				}
				if (StringUtils.equals(GraphDACParams.status.name(), entry.getKey()))
					statusFilter = true;
			}
		}

		if (!statusFilter && !traversal) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put(CompositeSearchParams.propertyName.name(), GraphDACParams.status.name());
			property.put(CompositeSearchParams.values.name(), Arrays.asList(new String[] { "Live" }));
			properties.add(property);
		}
		return properties;
	}

	private Map<String, Object> getCompositeSearchResponse(Map<String, Object> searchResponse) {
		Map<String, Object> respResult = new HashMap<String, Object>();
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
										respResult.put(key, list);
									}
									list.add(map);
								}
							}
						}
					}
				}
			} else {
				respResult.put(entry.getKey(), entry.getValue());
			}
		}
		return respResult;
	}

	@CoverageIgnore
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
				return objectType;
		}
		return null;
	}
}