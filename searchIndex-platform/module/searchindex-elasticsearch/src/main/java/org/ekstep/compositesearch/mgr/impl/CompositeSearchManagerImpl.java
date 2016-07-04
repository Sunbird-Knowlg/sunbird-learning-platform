package org.ekstep.compositesearch.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.mgr.BaseCompositeSearchManager;
import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.dac.enums.GraphDACParams;

@Component
public class CompositeSearchManagerImpl extends BaseCompositeSearchManager implements ICompositeSearchManager {
	
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
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> searchForTraversal(Request request) {
		SearchProcessor processor = new SearchProcessor();
		try {
			SearchDTO searchDTO = getSearchDTO(request);
			searchDTO.addAdditionalProperty("weightages", (Map<String, Double>) request.get("weightages"));
			searchDTO.addAdditionalProperty("graphId", (String) request.get("graphId"));
			Map<String,Object> lstResult = processor.processSearch(searchDTO, true);
			return lstResult;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
			if (null != req.get(CompositeSearchParams.limit.name())) {
				limit = (int) req.get(CompositeSearchParams.limit.name());
			}
			Boolean traversal = (Boolean) request.get("traversal");
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
			properties.addAll(getSearchFilterProperties(filters, traversal));
			searchObj.setSortBy(sortBy);
			searchObj.setFacets(facets);
			searchObj.setProperties(properties);
			searchObj.setLimit(limit);
			searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
			
			if(traversal != null){
				searchObj.setTraversalSearch(traversal);
			}
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map<String, Object>> getSearchFilterProperties(Map<String, Object> filters, Boolean traversal) throws Exception {
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		boolean statusFilter = false;
		if (null != filters && !filters.isEmpty()) { 
			for (Entry<String, Object> entry: filters.entrySet()) {
				Object filterObject = entry.getValue();
				if(filterObject instanceof Map){
					Map<String, Object> filterMap = (Map<String, Object>) filterObject;
					if(!filterMap.containsKey(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MIN) && !filterMap.containsKey(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MAX)){
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
					} else {
						Map<String, Object> property = new HashMap<String, Object>();
						Map<String, Object> rangeMap = new HashMap<String, Object>();
						Object minFilterValue = filterMap.get(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MIN);
						if(minFilterValue != null){
							rangeMap.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE, minFilterValue);
						}
						Object maxFilterValue = filterMap.get(CompositeSearchConstants.SEARCH_OPERATION_RANGE_MAX);
						if(maxFilterValue != null){
							rangeMap.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE, maxFilterValue);
						}	
						property.put(CompositeSearchParams.values.name(), rangeMap);
						property.put(CompositeSearchParams.propertyName.name(), entry.getKey());
						property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_RANGE);
						properties.add(property);
					}
				}
				else{
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
					property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
					properties.add(property);
				}
				}
				if (StringUtils.equals(GraphDACParams.status.name(), entry.getKey()))
				    statusFilter = true;
			}
		}
		if (!objTypeFilter && !traversal) {
		    Map<String, Object> property = new HashMap<String, Object>();
		    property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
            property.put(CompositeSearchParams.propertyName.name(), GraphDACParams.objectType.name());
            String[] objectTypes = new String[]{"Domain", "Dimension", "Concept", "Content", "Word", "Method", "Misconception", "AssessmentItem"};
            property.put(CompositeSearchParams.values.name(), Arrays.asList(objectTypes));
            properties.add(property);
		}
		if (!statusFilter && !traversal) {
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

}
