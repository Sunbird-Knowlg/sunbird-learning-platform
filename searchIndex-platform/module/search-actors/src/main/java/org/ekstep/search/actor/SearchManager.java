package org.ekstep.search.actor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.Recover;
import akka.util.Timeout;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.enums.Modes;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ObjectDefinitionCache;
import org.ekstep.telemetry.logger.TelemetryManager;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class SearchManager extends SearchBaseActor {

	private ObjectMapper mapper = new ObjectMapper();
	private static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30000, TimeUnit.MILLISECONDS));

	public Future<Response> onReceive(Request request) throws Throwable {
		String operation = request.getOperation();
		SearchProcessor processor = new SearchProcessor();
		try {
			if (StringUtils.equalsIgnoreCase(SearchOperations.INDEX_SEARCH.name(), operation)) {
				SearchDTO searchDTO = getSearchDTO(request);
				Future<Map<String, Object>> searchResult = processor.processSearch(searchDTO, true);
				return searchResult.map(new Mapper<Map<String, Object>, Response>() {
					@Override
					public Response apply(Map<String, Object> lstResult) {
						String mode = (String) request.getRequest().get(CompositeSearchParams.mode.name());
						if (StringUtils.isNotBlank(mode) && StringUtils.equalsIgnoreCase("collection", mode)) {
							return OK(getCollectionsResult(lstResult, processor, request));
						} else {
							return OK(lstResult);
						}
					}
				}, getContext().dispatcher()).recoverWith(new Recover<Future<Response>>() {
					@Override
					public Future<Response> recover(Throwable failure) throws Throwable {
						TelemetryManager.error("Unable to process the request:: Request: " + mapper.writeValueAsString(request), failure);
						return ERROR(request.getOperation(), failure);
					}
				}, getContext().dispatcher());
			} else if (StringUtils.equalsIgnoreCase(SearchOperations.COUNT.name(), operation)) {
				Map<String, Object> countResult = processor.processCount(getSearchDTO(request));
				if (null != countResult.get("count")) {
					Integer count = (Integer) countResult.get("count");
					return Futures.successful(OK("count", count));
				} else {
					return Futures.successful(ERROR("", "count is empty or null", ResponseCode.SERVER_ERROR, "", null));
				}
			} else if (StringUtils.equalsIgnoreCase(SearchOperations.METRICS.name(), operation)) {
				Future<Map<String, Object>> searchResult = processor.processSearch(getSearchDTO(request), false);
				return searchResult.map(new Mapper<Map<String, Object>, Response>() {
					@Override
					public Response apply(Map<String, Object> lstResult) {
						return OK(getCompositeSearchResponse(lstResult));
					}
				}, getContext().dispatcher());
			} else if (StringUtils.equalsIgnoreCase(SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name(), operation)) {
				Map<String, Object> searchResponse = (Map<String, Object>) request.get("searchResult");
				return Futures.successful(OK(getCompositeSearchResponse(searchResponse)));
			} else if (StringUtils.equalsIgnoreCase(SearchOperations.MULTI_LANGUAGE_WORD_SEARCH.name(), operation)) {
				List<String> synsetIdList = (List<String>) request.get("synset_id_list");
				Map<String, Object> lstResult = processor.multiWordDocSearch(synsetIdList);
				return Futures.successful(OK(lstResult));
			} else if (StringUtils.equalsIgnoreCase(SearchOperations.MULTI_LANGUAGE_SYNSET_SEARCH.name(), operation)) {
				List<String> synsetIdList = (List<String>) request.get("synset_ids");
				Map<String, Object> lstResult = processor.multiSynsetDocSearch(synsetIdList);
				return Futures.successful(OK(lstResult));
			} else {
				TelemetryManager.info("Invalid Request :: Unsupported operation: " , request.getRequest());
				return Futures.successful(ERROR(CompositeSearchErrorCodes.ERR_INVALID_OPERATION.name(), "Unsupported operation: " + operation, ResponseCode.CLIENT_ERROR, "", null));
			}
		} catch (Exception e) {
			TelemetryManager.info("Error while processing the request: REQUEST::" + mapper.writeValueAsString(request));
			return ERROR(operation, e);
		}
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SearchDTO getSearchDTO(Request request) throws Exception {
		SearchDTO searchObj = new SearchDTO();
		try {
			Map<String, Object> req = request.getRequest();
			TelemetryManager.log("Search Request: ", req);
			String queryString = (String) req.get(CompositeSearchParams.query.name());
			int limit = getIntValue(req.get(CompositeSearchParams.limit.name()));
			Boolean fuzzySearch = (Boolean) request.get("fuzzy");
			if (null == fuzzySearch)
				fuzzySearch = false;
			Boolean wordChainsRequest = (Boolean) request.get("traversal");
			if (null == wordChainsRequest)
				wordChainsRequest = false;
			List<Map> properties = new ArrayList<Map>();
			Map<String, Object> filters = (Map<String, Object>) req.get(CompositeSearchParams.filters.name());
			if (null == filters)
				filters = new HashMap<>();
			if (filters.containsKey("tags")) {
				Object tags = filters.get("tags");
				if (null != tags) {
					filters.remove("tags");
					filters.put("keywords", tags);
				}
			}
			if (filters.containsKey("relatedBoards"))
				filters.remove("relatedBoards");

			Object objectTypeFromFilter = filters.get(CompositeSearchParams.objectType.name());
			String objectType = null;
			if (objectTypeFromFilter != null) {
				if (objectTypeFromFilter instanceof List) {
					List objectTypeList = (List) objectTypeFromFilter;
					if (objectTypeList.size() > 0)
						objectType = (String) objectTypeList.get(0);
				} else if (objectTypeFromFilter instanceof String) {
					objectType = (String) objectTypeFromFilter;
				}
			}

			Object graphIdFromFilter = filters.get(CompositeSearchParams.graph_id.name());
			String graphId = null;
			if (graphIdFromFilter != null) {
				if (graphIdFromFilter instanceof List) {
					List graphIdList = (List) graphIdFromFilter;
					if (graphIdList.size() > 0)
						graphId = (String) graphIdList.get(0);
				} else if (graphIdFromFilter instanceof String) {
					graphId = (String) graphIdFromFilter;
				}
			}
			if (fuzzySearch && filters != null) {
				Map<String, Float> weightagesMap = new HashMap<String, Float>();
				weightagesMap.put("default_weightage", 1.0f);

				if (StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(graphId)) {
					Map<String, Object> objDefinition = ObjectDefinitionCache.getMetaData(objectType, graphId);
					// DefinitionDTO objDefinition =
					// DefinitionCache.getDefinitionNode(graphId, objectType);
					String weightagesString = (String) objDefinition.get("weightages");
					if (StringUtils.isNotBlank(weightagesString)) {
						weightagesMap = getWeightagesMap(weightagesString);
					}
				}
				searchObj.addAdditionalProperty("weightagesMap", weightagesMap);
			}

			List<String> exists = null;
			Object existsObject = req.get(CompositeSearchParams.exists.name());
			if (existsObject instanceof List) {
				exists = (List<String>) existsObject;
			} else if (existsObject instanceof String) {
				exists = new ArrayList<String>();
				exists.add((String) existsObject);
			}

			List<String> notExists = null;
			Object notExistsObject = req.get(CompositeSearchParams.not_exists.name());
			if (notExistsObject instanceof List) {
				notExists = (List<String>) notExistsObject;
			} else if (notExistsObject instanceof String) {
				notExists = new ArrayList<String>();
				notExists.add((String) notExistsObject);
			}

			Map<String, Object> softConstraints = null;
			if (null != req.get(CompositeSearchParams.softConstraints.name())) {
				softConstraints = (Map<String, Object>) req.get(CompositeSearchParams.softConstraints.name());
			}

			String mode = (String) req.get(CompositeSearchParams.mode.name());
			if (null != mode && mode.equals(Modes.soft.name())
					&& (null == softConstraints || softConstraints.isEmpty()) && objectType != null) {
				try {
					Map<String, Object> metaData = ObjectDefinitionCache.getMetaData(objectType);
					if (null != metaData.get("softConstraints")) {
						String constraintString = (String) metaData.get("softConstraints");
						softConstraints = mapper.readValue(constraintString, Map.class);
					}
				} catch (Exception e) {
					TelemetryManager.warn("Invalid soft Constraints" + e.getMessage());
				}
			}
			TelemetryManager.log("Soft Constraints with only Mode: ", softConstraints);
			if (null != softConstraints && !softConstraints.isEmpty()) {
				Map<String, Object> softConstraintMap = new HashMap<>();
				TelemetryManager.log("SoftConstraints:", softConstraints);
				try {
					for (String key : softConstraints.keySet()) {
						if (filters.containsKey(key) && null != filters.get(key)) {
							List<Object> data = new ArrayList<>();
							Integer boost = 1;
							Object boostValue = softConstraints.get(key);
							if (null != boostValue) {
								try {
									boost = Integer.parseInt(boostValue.toString());
								} catch (Exception e) {
									boost = 1;
								}
							}
							data.add(boost);
							if (filters.get(key) instanceof Map) {
								data.add(((Map) filters.get(key)).values().toArray()[0]);
							} else {
								data.add(filters.get(key));
							}

							softConstraintMap.put(key, data);
							filters.remove(key);
						}
					}
				} catch (Exception e) {
					TelemetryManager.warn("Invalid soft Constraints: " + e.getMessage());
				}
				if (MapUtils.isNotEmpty(softConstraintMap) && softConstraintMap.containsKey("board"))
					softConstraintMap.put("relatedBoards", softConstraintMap.get("board"));
				searchObj.setSoftConstraints(softConstraintMap);
			}
			TelemetryManager.log("SoftConstraints" + searchObj.getSoftConstraints());

			List<String> fieldsSearch = getList(req.get(CompositeSearchParams.fields.name()));
			List<String> facets = getList(req.get(CompositeSearchParams.facets.name()));
			Map<String, String> sortBy = (Map<String, String>) req.get(CompositeSearchParams.sort_by.name());
			properties.addAll(getAdditionalFilterProperties(exists, CompositeSearchParams.exists.name()));
			properties.addAll(getAdditionalFilterProperties(notExists, CompositeSearchParams.not_exists.name()));
			// Changing fields to null so that search all fields but returns
			// only the fields specified
			properties.addAll(getSearchQueryProperties(queryString, null));
			properties.addAll(getSearchFilterProperties(filters, wordChainsRequest));
			searchObj.setSortBy(sortBy);
			searchObj.setFacets(facets);
			searchObj.setProperties(properties);
			// Added Implicit Filter Properties To Support Collection content tagging to reuse by tenants.
			setImplicitFilters(filters, searchObj);
			searchObj.setLimit(limit);
			searchObj.setFields(fieldsSearch);
			searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
			getAggregations(req, searchObj);

			if (null != req.get(CompositeSearchParams.offset.name())) {
				int offset = getIntValue(req.get(CompositeSearchParams.offset.name()));
				TelemetryManager.log("Offset: " + offset);
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

	private Map<String, Float> getWeightagesMap(String weightagesString)
			throws JsonParseException, JsonMappingException, IOException {
		Map<String, Float> weightagesMap = new HashMap<String, Float>();
		if (weightagesString != null && !weightagesString.isEmpty()) {
			Map<String, Object> weightagesRequestMap = mapper.readValue(weightagesString,
					new TypeReference<Map<String, Object>>() {
					});

			for (Map.Entry<String, Object> entry : weightagesRequestMap.entrySet()) {
				Float weightage = Float.parseFloat(entry.getKey());
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

	@SuppressWarnings("unchecked")
	private List<String> getList(Object param) {
		List<String> paramList;
		try {
			paramList = (List<String>) param;
		} catch (Exception e) {
			String str = (String) param;
			paramList = new ArrayList<String>();
			paramList.add(str);
		}
		return paramList;
	}
	
private Integer getIntValue(Object num) {
	int i = 100;
	if (null != num) {
		try {
			i = (int) num;
		} catch (Exception e) {
			if(num instanceof String){
				try{
					return Integer.parseInt((String) num);
				}catch (Exception ex){
					throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_INVALID_PARAMS.name(), "Invalid Input.", e);
				}
			}
			i = new Long(num.toString()).intValue();
		}
	}
	return i;
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
		boolean publishedStatus = false;
		if (null != filters && !filters.isEmpty()) {
			publishedStatus = checkPublishedStatus(filters);
			for (Entry<String, Object> entry : filters.entrySet()) {
				if ("identifier".equalsIgnoreCase(entry.getKey())) {
					List ids = new ArrayList<>();
					if (entry.getValue() instanceof String) {
						ids.add(entry.getValue());
					} else {
						ids = (List<String>) entry.getValue();
					}
					List<String> identifiers = new ArrayList<>();
					identifiers.addAll((List<String>) (List<?>) ids);
					if(!publishedStatus){
						for (Object id : ids) {
							identifiers.add(id + ".img");
						}
					}
					entry.setValue(identifiers);
				}
				if (CompositeSearchParams.objectType.name().equals(entry.getKey())) {
					List value = new ArrayList<>();
					if (entry.getValue() instanceof String) {
						value.add(entry.getValue());
					} else {
						value = (List<String>) entry.getValue();
					}
					List<String> objectTypes = new ArrayList<>();
					objectTypes.addAll((List<String>) (List<?>) value);

					for (Object val : value) {
						if(StringUtils.equalsIgnoreCase("Content", (String) val) && !publishedStatus)
							objectTypes.add(val + "Image");
					}
					entry.setValue(objectTypes);
				}
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
							case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_OPERATOR:
							case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_TEXT:
							case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_TEXT_LOWERCASE:
							case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_TEXT_UPPERCASE:
								property.put(CompositeSearchParams.operation.name(),
										CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL);
								break;
							case CompositeSearchConstants.SEARCH_OPERATION_NOT_IN_OPERATOR:
								property.put(CompositeSearchParams.operation.name(),
										CompositeSearchConstants.SEARCH_OPERATION_NOT_IN);
								break;
							case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN:
							case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS:
							case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS:
							case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN: {
								property.put(CompositeSearchParams.operation.name(), filterEntry.getKey());
								break;
							}
							case "value":
							case CompositeSearchConstants.SEARCH_OPERATION_CONTAINS_OPERATOR: {
								property.put(CompositeSearchParams.operation.name(),
										CompositeSearchConstants.SEARCH_OPERATION_CONTAINS);
								break;
							}
							case CompositeSearchConstants.SEARCH_OPERATION_AND:
							case CompositeSearchConstants.SEARCH_OPERATION_AND_OPERATOR:
							case CompositeSearchConstants.SEARCH_OPERATION_AND_TEXT_LOWERCASE: {
								property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_AND);
								break;
							}
							default: {
								TelemetryManager.error("Invalid filters, Unsupported operation:: " + filterEntry.getKey() + ":: filters::" + filters);
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

				if (StringUtils.equals("status", entry.getKey()))
					statusFilter = true;
			}
		}

		if (!statusFilter && !traversal) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put(CompositeSearchParams.propertyName.name(), "status");
			property.put(CompositeSearchParams.values.name(), Arrays.asList(new String[] { "Live" }));
			properties.add(property);
		}
		return properties;
	}

	private boolean checkPublishedStatus(Map<String, Object> filters) {
		List<String> statuses = Arrays.asList("Live", "Unlisted");
		Object status =filters.get("status");
		List<String> statusList = null;
		if(null == status) {
			return true;
		} else if((status instanceof String) && (statuses.contains(status))){
			statusList = Arrays.asList((String) status);
		} else if(status instanceof String[]) {
			statusList = Arrays.asList((String[]) status);
		} else if(status instanceof List) {
			statusList = (List<String>) status;
		}

		if(CollectionUtils.isNotEmpty(statusList) && statusList.size() == 1 && statuses.contains(statusList.get(0)))
			return true;
		else if(CollectionUtils.isNotEmpty(statusList) && statuses.containsAll(statusList))
			return true;
		else
			return false;

	}

	@SuppressWarnings("unchecked")
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
							String objectType = (String) map.get("objectType");
							if (objectType.endsWith("Image")) {
								objectType = objectType.replace("Image", "");
								map.replace("objectType", objectType);
							}
							if (StringUtils.isNotBlank(objectType)) {
								String key = getResultParamKey(objectType);
								if (StringUtils.isNotBlank(key)) {
									List<Map<String, Object>> list = result.get(key);
									if (null == list) {
										list = new ArrayList<Map<String, Object>>();
										result.put(key, list);
										respResult.put(key, list);
									}
									String id = (String) map.get("identifier");
									if (id.endsWith(".img")) {
										id = id.replace(".img", "");
										map.replace("identifier", id);
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
			else if (StringUtils.equalsIgnoreCase("License", objectType))
				return "license";
			else
				return objectType;
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private boolean isEmpty(Object o) {
		boolean result = false;
		if (o instanceof String) {
			result = StringUtils.isBlank((String) o);
		} else if (o instanceof List) {
			result = ((List) o).isEmpty();
		} else if (o instanceof String[]) {
			result = (((String[]) o).length <= 0);
		}
		return result;
	}

	/**
	 * @param lstResult
	 * @param processor
	 * @param parentRequest
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> getCollectionsResult(Map<String, Object> lstResult, SearchProcessor processor,
			Request parentRequest) {
		List<Map> contentResults = (List<Map>) lstResult.get("results");
		if (null != contentResults && !contentResults.isEmpty()) {
			try {
				List<String> contentIds = new ArrayList<String>();
				for (Map<String, Object> content : contentResults) {
					contentIds.add((String) content.get("identifier"));
				}

				Request request = new Request();
				Map<String, Object> filters = new HashMap<String, Object>();
				List<String> objectTypes = new ArrayList<String>();
				objectTypes.add("Content");
				filters.put(CompositeSearchParams.objectType.name(), objectTypes);
				List<String> mimeTypes = new ArrayList<String>();
				mimeTypes.add("application/vnd.ekstep.content-collection");
				filters.put("mimeType", mimeTypes);
				filters.put("childNodes", contentIds);
				request.put(CompositeSearchParams.sort_by.name(),
						parentRequest.get(CompositeSearchParams.sort_by.name()));
				request.put(CompositeSearchParams.fields.name(),
						getCollectionFields(getList(parentRequest.get(CompositeSearchParams.fields.name()))));
				request.put(CompositeSearchParams.filters.name(), filters);
				SearchDTO searchDTO = getSearchDTO(request);
				Map<String, Object> collectionResult = Await.result(processor.processSearch(searchDTO, true),
						WAIT_TIMEOUT.duration());
				collectionResult = prepareCollectionResult(collectionResult, contentIds);
				lstResult.putAll(collectionResult);
				return lstResult;
			} catch (Exception e) {
				TelemetryManager.error("Error while fetching the collection for the contents : ", e);
				return lstResult;
			}

		} else {
			return lstResult;
		}
	}

	/**
	 * @param fieldlist
	 * @return
	 * @return
	 */
	private Object getCollectionFields(List<String> fieldlist) {
		List<String> fields = Platform.config.hasPath("search.fields.mode_collection")
				? Platform.config.getStringList("search.fields.mode_collection")
				: Arrays.asList("identifier", "name", "objectType", "contentType", "mimeType", "size", "childNodes");

		if (null != fieldlist && !fieldlist.isEmpty()) {
			fields.addAll(fieldlist);
			fields = fields.stream().distinct().collect(Collectors.toList());
		}
		return fields;
	}

	/**
	 * @param collectionResult
	 * @param contentIds
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> prepareCollectionResult(Map<String, Object> collectionResult, List<String> contentIds) {
		List<Map> results = new ArrayList<Map>();
		for (Map<String, Object> collection : (List<Map>) collectionResult.get("results")) {
			List<String> childNodes = (List<String>) collection.get("childNodes");
			childNodes = (List<String>) CollectionUtils.intersection(childNodes, contentIds);
			collection.put("childNodes", childNodes);
			results.add(collection);
		}
		collectionResult.put("collections", results);
		collectionResult.put("collectionsCount", collectionResult.get("count"));
		collectionResult.remove("count");
		collectionResult.remove("results");
		return collectionResult;
	}

	private void getAggregations(Map<String, Object> req, SearchDTO searchObj) {
		if(null != req.get("aggregations") && CollectionUtils.isNotEmpty((List<Map<String, Object>>) req.get("aggregations"))){
			searchObj.setAggregations((List<Map<String, Object>>) req.get("aggregations"));
		}

	}

	private void setImplicitFilters(Map<String, Object> filters, SearchDTO searchObj) throws Exception {
		Map<String, Object> implicitFilter = new HashMap<String, Object>();
		if (MapUtils.isNotEmpty(filters) && filters.containsKey("board")) {
			for (String key : filters.keySet()) {
				if (StringUtils.equalsIgnoreCase("board", key)) {
					implicitFilter.put("relatedBoards", filters.get(key));
				} else if (StringUtils.equalsIgnoreCase("status", key)) {
					implicitFilter.put("status", "Live");
				} else {
					implicitFilter.put(key, filters.get(key));
				}
			}
			List<Map> implicitFilterProps = new ArrayList<Map>();
			implicitFilterProps.addAll(getSearchFilterProperties(implicitFilter, false));
			searchObj.setImplicitFilterProperties(implicitFilterProps);
		}
	}

}