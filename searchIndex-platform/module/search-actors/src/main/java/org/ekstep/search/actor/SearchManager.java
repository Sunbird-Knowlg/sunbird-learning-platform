package org.ekstep.search.actor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.enums.Modes;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

import com.ilimi.common.dto.CoverageIgnore;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;

import akka.actor.ActorRef;

public class SearchManager extends SearchBaseActor {

	private static ILogger LOGGER = new PlatformLogger(SearchManager.class.getName());

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

			} else if (StringUtils.equalsIgnoreCase(SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name(),
					operation)) {
				Map<String, Object> searchResponse = (Map<String, Object>) request.get("searchResult");
				OK(getCompositeSearchResponse(searchResponse), parent);

			} else if (StringUtils.equalsIgnoreCase(SearchOperations.MULTI_LANGUAGE_WORD_SEARCH.name(), operation)) {
				List<String> synsetIdList = (List<String>) request.get("synset_id_list");
				Map<String, Object> lstResult = processor.multiWordDocSearch(synsetIdList);
				OK(lstResult, parent);
			} else if (StringUtils.equalsIgnoreCase(SearchOperations.MULTI_LANGUAGE_SYNSET_SEARCH.name(), operation)) {
				List<String> synsetIdList = (List<String>) request.get("synset_ids");
				Map<String, Object> lstResult = processor.multiSynsetDocSearch(synsetIdList);
				OK(lstResult, parent);
			} else {
				LOGGER.log("Unsupported operation: " + operation);
				throw new ClientException(CompositeSearchErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log("Error in SearchManager actor", e.getMessage(), e);
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
			LOGGER.log("Search Request: " , req);
			String queryString = (String) req.get(CompositeSearchParams.query.name());
			int limit = getLimitValue(req.get(CompositeSearchParams.limit.name()));
			Boolean fuzzySearch = (Boolean) request.get("fuzzy");
			if (null == fuzzySearch)
				fuzzySearch = false;
			Boolean wordChainsRequest = (Boolean) request.get("traversal");
			if (null == wordChainsRequest)
				wordChainsRequest = false;
			List<Map> properties = new ArrayList<Map>();
			Map<String, Object> filters = (Map<String, Object>) req.get(CompositeSearchParams.filters.name());

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
				Map<String, Double> weightagesMap = new HashMap<String, Double>();
				weightagesMap.put("default_weightage", 1.0);

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
					&& (null == softConstraints || softConstraints.isEmpty())) {
				try {
					Map<String, Object> metaData = ObjectDefinitionCache.getMetaData(objectType);
					if (null != metaData.get("softConstraints")) {
						ObjectMapper mapper = new ObjectMapper();
						String constraintString = (String) metaData.get("softConstraints");
						softConstraints = mapper.readValue(constraintString, Map.class);
					}
				} catch (Exception e) {
				}
			}

			if (null != softConstraints && !softConstraints.isEmpty()) {
				Map<String, Object> softConstraintMap = new HashMap<>();
				LOGGER.log("SoftConstraints:" , softConstraints);
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
							data.add(filters.get(key));
							softConstraintMap.put(key, data);
							filters.remove(key);
						}
					}
				} catch (Exception e) {
					LOGGER.log("Invalid soft Constraints", e.getMessage(), e, "WARN");
				}
				searchObj.setSoftConstraints(softConstraintMap);
			}

			List<String> fieldsSearch = getList(req.get(CompositeSearchParams.fields.name()));
			LOGGER.log("Fields: " , fieldsSearch);
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
			searchObj.setLimit(limit);
			searchObj.setFields(fieldsSearch);
			searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);

			if (null != req.get(CompositeSearchParams.offset.name())) {
				int offset = (Integer) req.get(CompositeSearchParams.offset.name());
				LOGGER.log("Offset: " + offset);
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
	private Map<String, Double> getWeightagesMap(String weightagesString)
			throws JsonParseException, JsonMappingException, IOException {
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

	@SuppressWarnings("unchecked")
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

	private Integer getLimitValue(Object limit) {
		int i = 100;
		if (null != limit) {
			try {
				i = (int) limit;
			} catch (Exception e) {
				i = new Long(limit.toString()).intValue();
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
		boolean compatibilityFilter = false;
		boolean isContentSearch = false;
		boolean statusFilter = false;
		if (null != filters && !filters.isEmpty()) {
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
					for (Object id : ids) {
						identifiers.add(id + ".img");
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
				if (StringUtils.equals(CompositeSearchParams.objectType.name(), entry.getKey())) {
					String objectType = null;
					if (filterObject instanceof List) {
						List objectTypeList = (List) filterObject;
						if (objectTypeList.size() == 1)
							objectType = (String) objectTypeList.get(0);
					} else if (filterObject instanceof Object[]) {
						Object[] objectTypeList = (Object[]) filterObject;
						if (objectTypeList.length == 1)
							objectType = (String) objectTypeList[0];
					} else if (filterObject instanceof String) {
						objectType = (String) filterObject;
					}
					if (StringUtils.equalsIgnoreCase(CompositeSearchParams.Content.name(), objectType))
						isContentSearch = true;
				}
				if (StringUtils.equals(GraphDACParams.status.name(), entry.getKey()))
					statusFilter = true;
				if (StringUtils.equals(CompositeSearchParams.compatibilityLevel.name(), entry.getKey()))
					compatibilityFilter = true;
			}
		}

		if (!compatibilityFilter && isContentSearch && !traversal) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put(CompositeSearchParams.propertyName.name(), CompositeSearchParams.compatibilityLevel.name());
			property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put(CompositeSearchParams.values.name(), Arrays.asList(new Integer[] { 1 }));
			properties.add(property);
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

	@SuppressWarnings("unchecked")
	private Map<String, Object> getCompositeSearchResponse(Map<String, Object> searchResponse) {
		Map<String, Object> respResult = new HashMap<String, Object>();
		LOGGER.log("Logging search Response :" , searchResponse.entrySet());
		for (Map.Entry<String, Object> entry : searchResponse.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("results")) {
				List<Object> lstResult = (List<Object>) entry.getValue();
				if (null != lstResult && !lstResult.isEmpty()) {
					Map<String, List<Map<String, Object>>> result = new HashMap<String, List<Map<String, Object>>>();
					for (Object obj : lstResult) {
						if (obj instanceof Map) {
							Map<String, Object> map = (Map<String, Object>) obj;
							String objectType = (String) map.get(GraphDACParams.objectType.name());
							if (objectType.endsWith("Image")) {
								objectType = objectType.replace("Image", "");
								map.replace(GraphDACParams.objectType.name(), objectType);
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
									String es_id = (String) map.get("es_metadata_id");
									if (es_id.endsWith(".img")) {
										es_id = es_id.replace(".img", "");
										map.replace("es_metadata_id", es_id);
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
		LOGGER.log("Search Result", respResult);
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
}