package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.transformer.AggregationsResultTransformer;
import org.ekstep.searchindex.util.CompositeSearchConstants;

import com.google.gson.internal.LinkedTreeMap;

import io.searchbox.core.CountResult;
import io.searchbox.core.SearchResult;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class SearchProcessor {

	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> processSearch(SearchDTO searchDTO, boolean includeResults) throws Exception {
		List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
		Map<String, Object> response = new HashMap<String, Object>();
		
		String query = processSearchQuery(searchDTO, groupByFinalList, true);
		SearchResult searchResult = elasticSearchUtil.search(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, query);
		
		if (searchDTO.isFuzzySearch()) {
			List<Map> results = elasticSearchUtil.getDocumentsFromSearchResultWithScore(searchResult);
			response.put("results", results);
			return response;
		}
		if (includeResults) {
		    List<Object> results = elasticSearchUtil.getDocumentsFromSearchResult(searchResult, Map.class);
	        response.put("results", results);
		}
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) searchResult
				.getValue("aggregations");
		if (aggregations != null && !aggregations.isEmpty()) {
			AggregationsResultTransformer transformer =  new AggregationsResultTransformer();
			response.put("facets", (List<Map<String, Object>>)elasticSearchUtil.getCountFromAggregation(aggregations, groupByFinalList, transformer));
		}
		response.put("count", searchResult.getTotal());
		return response;
	}
	
	public Map<String, Object> processCount(SearchDTO searchDTO) throws Exception {
		Map<String, Object> response = new HashMap<String, Object>();
		String query = processSearchQuery(searchDTO, null, false);
		
		CountResult countResult = elasticSearchUtil.count(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, query);
		response.put("count", countResult.getCount());
		
		return response;
	}
	
	public void destroy() {
		elasticSearchUtil.finalize();
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String processSearchQuery(SearchDTO searchDTO, List<Map<String, Object>> groupByFinalList, boolean sort) throws Exception{
		List<Map> conditionsSetOne = new ArrayList<Map>();
		List<Map> conditionsSetArithmetic = new ArrayList<Map>();
		List<Map> conditionsSetMustNot = new ArrayList<Map>();
		Map<String, List> conditionsMap = new HashMap<String, List>();
		conditionsMap.put(CompositeSearchConstants.CONDITION_SET_MUST, conditionsSetOne);
		conditionsMap.put(CompositeSearchConstants.CONDITION_SET_ARITHMETIC, conditionsSetArithmetic);
		conditionsMap.put(CompositeSearchConstants.CONDITION_SET_MUST_NOT, conditionsSetMustNot);
		boolean relevanceSort = false;
		List<Map> properties = searchDTO.getProperties();

		String totalOperation = searchDTO.getOperation();
		for (Map<String, Object> property : properties) {
			String propertyName = (String) property.get("propertyName");
			if (propertyName.equals("*")) {
				propertyName = "all_fields";
			}
			String operation = (String) property.get("operation");
			List<Object> values;
			try {
				values = (List<Object>) property.get("values");
			} catch (Exception e) {
				values = Arrays.asList(property.get("values"));
			}
			String queryOperation = null;
			String conditionSet = null;
			switch (operation) {
			case CompositeSearchConstants.SEARCH_OPERATION_EQUAL: {
				queryOperation = "equal";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL: {
				queryOperation = "equal";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST_NOT;
				break;
			}

			case CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH: {
				queryOperation = "endsWith";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LIKE:
			case CompositeSearchConstants.SEARCH_OPERATION_CONTAINS: {
				queryOperation = "like";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_LIKE: {
				queryOperation = "like";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST_NOT;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH: {
				queryOperation = "prefix";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_EXISTS: {
				queryOperation = "exists";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EXISTS: {
				queryOperation = "exists";
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST_NOT;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN: {
				queryOperation = ">";
				conditionSet = CompositeSearchConstants.CONDITION_SET_ARITHMETIC;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				queryOperation = ">=";
				conditionSet = CompositeSearchConstants.CONDITION_SET_ARITHMETIC;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN: {
				queryOperation = "<";
				conditionSet = CompositeSearchConstants.CONDITION_SET_ARITHMETIC;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				queryOperation = "<=";
				conditionSet = CompositeSearchConstants.CONDITION_SET_ARITHMETIC;
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_RANGE: {
				queryOperation = CompositeSearchConstants.SEARCH_OPERATION_RANGE;
				conditionSet = CompositeSearchConstants.CONDITION_SET_MUST;
				break;
			}
			}

			Map<String, Object> condition = new HashMap<String, Object>();
			if (values.size() > 1) {
				condition.put("operation", "bool");
				condition.put("operand", "should");
				ArrayList<Map> subConditions = new ArrayList<Map>();
				for (Object value : values) {
					Map<String, Object> subCondition = new HashMap<String, Object>();
					subCondition.put("operation", queryOperation);
					subCondition.put("fieldName", propertyName);
					subCondition.put("value", value);
					subConditions.add(subCondition);
				}
				condition.put("subConditions", subConditions);
			} else if (propertyName.equalsIgnoreCase("all_fields")) {
			    relevanceSort = true;
				List<String> queryFields = elasticSearchUtil.getQuerySearchFields();
				condition.put("operation", "bool");
				condition.put("operand", "should");
				Map<String, Object> queryCondition = new HashMap<String, Object>();
				queryCondition.put("operation", queryOperation);
                queryCondition.put("fields", queryFields);
                queryCondition.put("value", values.get(0));
				condition.put("queryCondition", queryCondition);
			} else {
				condition.put("operation", queryOperation);
				condition.put("fieldName", propertyName);
				condition.put("value", values.get(0));
			}
			conditionsMap.get(conditionSet).add(condition);
		}

		if (searchDTO.getFacets() != null && groupByFinalList != null) {
			for (String facet : searchDTO.getFacets()) {
				Map<String, Object> groupByMap = new HashMap<String, Object>();
				groupByMap.put("groupByParent", facet);
				groupByFinalList.add(groupByMap);
			}
		}
		elasticSearchUtil.setResultLimit(searchDTO.getLimit());
		
		if(sort && !relevanceSort){
			Map<String, String> sortBy = searchDTO.getSortBy();
			if(sortBy == null || sortBy.isEmpty()){
				sortBy = new HashMap<String, String>();
				sortBy.put("name", "asc");
				sortBy.put("lastUpdatedOn", "desc");
				searchDTO.setSortBy(sortBy);
			}
		}
		String query;
		if(searchDTO.isFuzzySearch()){
			Map<String, Object> baseConditions = (Map<String, Object>) searchDTO.getAdditionalProperty("baseConditions");
			query = makeElasticSearchQueryWithFilteredSubsets(conditionsMap, totalOperation, groupByFinalList, searchDTO.getSortBy(), baseConditions);
		}
		else
		{
			query = makeElasticSearchQuery(conditionsMap, totalOperation, groupByFinalList, searchDTO.getSortBy());
		}
		return query;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String makeElasticSearchQueryWithFilteredSubsets(Map<String, List> conditionsMap, String totalOperation,
			List<Map<String, Object>> groupByList, Map<String, String> sortBy, Map<String,  Object> baseConditions) throws Exception {
		
		JSONBuilder builder = new JSONStringer();
		builder.object();
		List<Map> mustConditions = conditionsMap.get(CompositeSearchConstants.CONDITION_SET_MUST);
		List<Map> arithmeticConditions = conditionsMap.get(CompositeSearchConstants.CONDITION_SET_ARITHMETIC);
		List<Map> notConditions = conditionsMap.get(CompositeSearchConstants.CONDITION_SET_MUST_NOT);
		Map<String,  Double> weightages = (Map<String, Double>) baseConditions.get("weightages");

			builder.key("query").object()
				.key("function_score")
					.object()
						.key("query")
							.object()
								.key("bool").object()
									.key("must").array();
										
			for(Map.Entry<String, Object> entry: baseConditions.entrySet()){
				if(!entry.getKey().equalsIgnoreCase("weightages")){
					String field = entry.getKey();
					
					if( entry.getValue() instanceof List){
						builder.object()
							.key("bool").object()
								.key("should").array();
						List<String> values = (List<String>) entry.getValue();
						for(String value: values){
							builder.object()
								.key("match").object()
									.key(field + CompositeSearchConstants.RAW_FIELD_EXTENSION).value(value)
								.endObject()
							.endObject();
						}
								builder.endArray()
								.endObject()
							.endObject();
					}
					else {
						String value = (String) entry.getValue();
						builder.object()
							.key("match").object()
								.key(field + CompositeSearchConstants.RAW_FIELD_EXTENSION).value(value)
							.endObject()
						.endObject();
					}
				}
			}
										
						builder.endArray()
					.endObject()
				.endObject()
			.key("functions").array();

		if (mustConditions != null && !mustConditions.isEmpty()) {
			for (Map textCondition : mustConditions) {
				builder.object().key("filter").object();
				String conditionOperation = (String) textCondition.get("operation");
				Double weight = weightages.get("default_weightage");
				if (conditionOperation.equalsIgnoreCase("bool")) {
					String operand = (String) textCondition.get("operand");
					builder.key("bool").object();
					builder.key(operand).array();
					List<Map> subConditions = (List<Map>) textCondition.get("subConditions");
					if (null != subConditions && !subConditions.isEmpty()) {
					    for (Map subCondition : subConditions) {
	                        builder.object();
	                        String queryOperation = (String) subCondition.get("operation");
	                        String fieldName = (String) subCondition.get("fieldName");
	                        Object value = subCondition.get("value");
	                        getConditionsQuery(queryOperation, fieldName, value, builder);
	                        builder.endObject();
	                        if(weightages.containsKey(fieldName)){
	                        	weight = weightages.get(fieldName);
	                        }
	                    }
					} 
					builder.endArray();
					builder.endObject();
				} else {
					String queryOperation = (String) textCondition.get("operation");
					String fieldName = (String) textCondition.get("fieldName");
					Object value = (Object) textCondition.get("value");
					getConditionsQuery(queryOperation, fieldName, value, builder);
					if(weightages.containsKey(fieldName)){
                    	weight = weightages.get(fieldName);
                    }
				}
				builder.endObject().key("weight").value(weight).endObject();
			}
		}

		if (arithmeticConditions != null && !arithmeticConditions.isEmpty()) {
			for (Map arithmeticCondition : arithmeticConditions) {
				builder.object().key("filter").object().key("script").object().key("script");
				String conditionOperation = (String) arithmeticCondition.get("operation");
				String conditionScript = "";
				Double weight = weightages.get("default_weightage");
				if (conditionOperation.equalsIgnoreCase("bool")) {
					String operand = "||";
					StringBuffer finalScript = new StringBuffer();
					finalScript.append("(");
					List<Map> subConditions = (List<Map>) arithmeticCondition.get("subConditions");
					List<String> scripts = new ArrayList<String>();
					for (Map subCondition : subConditions) {
						StringBuffer script = new StringBuffer();
						String queryOperation = (String) subCondition.get("operation");
						String fieldName = (String) subCondition.get("fieldName");
						Object value = (Object) subCondition.get("value");
						script.append("doc['").append(fieldName).append("']").append(".value ").append(queryOperation)
								.append(" ").append(value);
						scripts.add(script.toString());
						if(weightages.containsKey(fieldName)){
                        	weight = weightages.get(fieldName);
                        }
					}
					String tempScript = "";
					for (String script : scripts) {
						tempScript = tempScript + operand + script;
					}
					tempScript = tempScript.substring(2);
					finalScript.append(tempScript).append(")");
					conditionScript = finalScript.toString();
				} else {
					StringBuffer script = new StringBuffer();
					String queryOperation = (String) arithmeticCondition.get("operation");
					String fieldName = (String) arithmeticCondition.get("fieldName");
					Object value = (Object) arithmeticCondition.get("value");
					script.append("doc['").append(fieldName).append("']").append(".value ").append(queryOperation)
							.append(" ").append(value);
					conditionScript = script.toString();
					if(weightages.containsKey(fieldName)){
                    	weight = weightages.get(fieldName);
                    }
				}
				builder.value(conditionScript).endObject().endObject().key("weight").value(weight).endObject();
			}
		}

		if (notConditions != null && !notConditions.isEmpty()) {
			String allOperation = "must_not";
			for (Map notCondition : notConditions) {
				
				builder.object().key("filter").object().key("bool").object().key(allOperation).object();
				Double weight = weightages.get("default_weightage");
				String conditionOperation = (String) notCondition.get("operation");
				if (conditionOperation.equalsIgnoreCase("bool")) {
					String operand = (String) notCondition.get("operand");
					builder.key("bool").object();
					builder.key(operand).array();
					List<Map> subConditions = (List<Map>) notCondition.get("subConditions");
					for (Map subCondition : subConditions) {
						builder.object();
						String queryOperation = (String) subCondition.get("operation");
						String fieldName = (String) subCondition.get("fieldName");
						Object value = subCondition.get("value");
						getConditionsQuery(queryOperation, fieldName, value, builder);
						builder.endObject();
						if(weightages.containsKey(fieldName)){
	                    	weight = weightages.get(fieldName);
	                    }
					}
					builder.endArray();
					builder.endObject();
					
				} else {
					String queryOperation = (String) notCondition.get("operation");
					String fieldName = (String) notCondition.get("fieldName");
					Object value = notCondition.get("value");
					getConditionsQuery(queryOperation, fieldName, value, builder);
					if(weightages.containsKey(fieldName)){
                    	weight = weightages.get(fieldName);
                    }
				}
				builder.endObject().endObject().endObject().key("weight").value(weight).endObject();
			}
		}

		builder.endArray().key("score_mode").value("sum").key("boost_mode").value("replace").endObject().endObject();

		builder.endObject();
		return builder.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String makeElasticSearchQuery(Map<String, List> conditionsMap, String totalOperation,
			List<Map<String, Object>> groupByList, Map<String, String> sortBy) throws Exception {
		JSONBuilder builder = new JSONStringer();
		builder.object();
		List<Map> mustConditions = conditionsMap.get(CompositeSearchConstants.CONDITION_SET_MUST);
		List<Map> arithmeticConditions = conditionsMap.get(CompositeSearchConstants.CONDITION_SET_ARITHMETIC);
		List<Map> notConditions = conditionsMap.get(CompositeSearchConstants.CONDITION_SET_MUST_NOT);

		if ((mustConditions != null && !mustConditions.isEmpty())
				|| (arithmeticConditions != null && !arithmeticConditions.isEmpty())
				|| (notConditions != null && !notConditions.isEmpty())) {
			builder.key("query").object().key("filtered").object().key("query").object().key("bool").object();
		}

		if (mustConditions != null && !mustConditions.isEmpty()) {
			String allOperation = "should";
			if (totalOperation == "AND") {
				allOperation = "must";
			}
			builder.key(allOperation).array();
			for (Map textCondition : mustConditions) {
				String conditionOperation = (String) textCondition.get("operation");
				if (conditionOperation.equalsIgnoreCase("bool")) {
					String operand = (String) textCondition.get("operand");
					builder.object().key("bool").object();
					builder.key(operand).array();
					List<Map> subConditions = (List<Map>) textCondition.get("subConditions");
					Map<String, Object> queryCondition = (Map<String, Object>) textCondition.get("queryCondition");
					if (null != subConditions && !subConditions.isEmpty()) {
					    for (Map subCondition : subConditions) {
	                        builder.object();
	                        String queryOperation = (String) subCondition.get("operation");
	                        String fieldName = (String) subCondition.get("fieldName");
	                        Object value = subCondition.get("value");
	                        getConditionsQuery(queryOperation, fieldName, value, builder);
	                        builder.endObject();
	                    }
					} else if (null != queryCondition && !queryCondition.isEmpty()) {
					    builder.object();
					    String queryOperation = (String) queryCondition.get("operation");
					    List<String> queryFields = (List<String>) queryCondition.get("fields");
					    Object value = queryCondition.get("value");
					    getConditionsQuery(queryOperation, queryFields, value, builder);
					    builder.endObject();
					}
					builder.endArray();
					builder.endObject().endObject();
				} else {
					builder.object();
					String queryOperation = (String) textCondition.get("operation");
					String fieldName = (String) textCondition.get("fieldName");
					Object value = (Object) textCondition.get("value");
					getConditionsQuery(queryOperation, fieldName, value, builder);
					builder.endObject();
				}
			}
			builder.endArray();
		}

		if (arithmeticConditions != null && !arithmeticConditions.isEmpty()) {
			String allOperation = "||";
			String scriptOperation = "should";
			if (totalOperation == "AND") {
				allOperation = "&&";
				scriptOperation = "must";
			}
			builder.key(scriptOperation).array();

			builder.object().key("script").object().key("script");
			String overallScript = "";
			for (Map arithmeticCondition : arithmeticConditions) {
				String conditionOperation = (String) arithmeticCondition.get("operation");
				String conditionScript = "";
				if (conditionOperation.equalsIgnoreCase("bool")) {
					String operand = "||";
					StringBuffer finalScript = new StringBuffer();
					finalScript.append("(");
					List<Map> subConditions = (List<Map>) arithmeticCondition.get("subConditions");
					List<String> scripts = new ArrayList<String>();
					for (Map subCondition : subConditions) {
						StringBuffer script = new StringBuffer();
						String queryOperation = (String) subCondition.get("operation");
						String fieldName = (String) subCondition.get("fieldName");
						Object value = (Object) subCondition.get("value");
						script.append("doc['").append(fieldName).append("']").append(".value ").append(queryOperation)
								.append(" ").append(value);
						scripts.add(script.toString());
					}
					String tempScript = "";
					for (String script : scripts) {
						tempScript = tempScript + operand + script;
					}
					tempScript = tempScript.substring(2);
					finalScript.append(tempScript).append(")");
					conditionScript = finalScript.toString();
				} else {
					StringBuffer script = new StringBuffer();
					String queryOperation = (String) arithmeticCondition.get("operation");
					String fieldName = (String) arithmeticCondition.get("fieldName");
					Object value = (Object) arithmeticCondition.get("value");
					script.append("doc['").append(fieldName).append("']").append(".value ").append(queryOperation)
							.append(" ").append(value);
					conditionScript = script.toString();
				}
				overallScript = overallScript + allOperation + conditionScript;
			}
			builder.value(overallScript.substring(2));
			builder.endObject().endObject().endArray();
		}

		if (notConditions != null && !notConditions.isEmpty()) {
			String allOperation = "must_not";
			builder.key(allOperation).array();
			for (Map notCondition : notConditions) {
				String conditionOperation = (String) notCondition.get("operation");
				if (conditionOperation.equalsIgnoreCase("bool")) {
					String operand = (String) notCondition.get("operand");
					builder.object().key("bool").object();
					builder.key(operand).array();
					List<Map> subConditions = (List<Map>) notCondition.get("subConditions");
					for (Map subCondition : subConditions) {
						builder.object();
						String queryOperation = (String) subCondition.get("operation");
						String fieldName = (String) subCondition.get("fieldName");
						Object value = subCondition.get("value");
						getConditionsQuery(queryOperation, fieldName, value, builder);
						builder.endObject();
					}
					builder.endArray();
					builder.endObject().endObject();
				} else {
					builder.object();
					String queryOperation = (String) notCondition.get("operation");
					String fieldName = (String) notCondition.get("fieldName");
					Object value = notCondition.get("value");
					getConditionsQuery(queryOperation, fieldName, value, builder);
					builder.endObject();
				}
			}
			builder.endArray();
		}
		
		if ((mustConditions != null && !mustConditions.isEmpty())
				|| (arithmeticConditions != null && !arithmeticConditions.isEmpty())
				|| (notConditions != null && !notConditions.isEmpty())) {
			builder.endObject().endObject().endObject().endObject();
		}

		if (groupByList != null && !groupByList.isEmpty()) {
			builder.key("aggs").object();
			for (Map<String, Object> groupByMap : groupByList) {
				String groupByParent = (String) groupByMap.get("groupByParent");
				builder.key(groupByParent).object().key("terms").object().key("field").value(groupByParent + CompositeSearchConstants.RAW_FIELD_EXTENSION).key("size")
						.value(elasticSearchUtil.defaultResultLimit).endObject().endObject();

				List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
				if (groupByChildList != null && !groupByChildList.isEmpty()) {
					builder.key("aggs").object();
					for (String childGroupBy : groupByChildList) {
						builder.key(childGroupBy).object().key("terms").object().key("field").value(childGroupBy + CompositeSearchConstants.RAW_FIELD_EXTENSION)
								.key("size").value(elasticSearchUtil.defaultResultLimit).endObject().endObject();
					}
					builder.endObject();
				}
			}
			builder.endObject();
		}
		
		if(sortBy != null && !sortBy.isEmpty()){
			builder.key("sort").array();
			List<String> dateFields = elasticSearchUtil.getDateFields();
			for(Map.Entry<String, String> entry: sortBy.entrySet()){
				String fieldName;
				if(dateFields.contains(entry.getKey())){
					fieldName = entry.getKey();
				}else{
					fieldName = entry.getKey() + CompositeSearchConstants.RAW_FIELD_EXTENSION;
				}
				builder.object().key(fieldName).value(entry.getValue()).endObject();
			}
			builder.endArray();
		}

		builder.endObject();
		return builder.toString();
	}
	
	@SuppressWarnings("unchecked")
	private void getConditionsQuery(String queryOperation, String fieldName, Object value, JSONBuilder builder) throws Exception {
		switch (queryOperation) {
		case "equal": {
			builder.key("match").object().key(fieldName + CompositeSearchConstants.RAW_FIELD_EXTENSION).value(value).endObject();
			break;
		}
		case "like": {
			builder.key("match").object().key(fieldName).object().key("query").value(value).key("operator").value("and").endObject().endObject();
			break;
		}
		case "prefix": {
			String stringValue = (String) value;
			builder.key("query").object().key("prefix").object().key(fieldName + CompositeSearchConstants.RAW_FIELD_EXTENSION)
					.value(stringValue.toLowerCase()).endObject().endObject();
			break;
		}
		case "exists": {
			builder.key("exists").object().key("field").value(value).endObject();
			break;
		}
		case "endsWith": {
			String stringValue = (String) value;
			builder.key("query").object().key("wildcard").object().key(fieldName + CompositeSearchConstants.RAW_FIELD_EXTENSION)
					.value("*" + stringValue.toLowerCase()).endObject().endObject();
			break;
		}
		case CompositeSearchConstants.SEARCH_OPERATION_RANGE: {
			Map<String, Object> rangeMap = (Map<String, Object>) value;
				if(!rangeMap.isEmpty()){
				List<String> dateFields = elasticSearchUtil.getDateFields();
				if(!dateFields.contains(fieldName)){
					fieldName = fieldName + CompositeSearchConstants.RAW_FIELD_EXTENSION;
				}
				builder.key("query").object().key("range").object().key(fieldName).object();
				for(Map.Entry<String, Object> rangeEntry: rangeMap.entrySet()){
/*					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					SimpleDateFormat esFromatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+"+elasticSearchUtil.getTimeZone()+"'");
					Object rangeValue;
					try{
						String dateString = (String)rangeEntry.getValue();
						if(dateString.split(" ").length>1){
							formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						}
						try {
							Date date = formatter.parse((String)rangeEntry.getValue());
							rangeValue = esFromatter.format(date);
						} catch (ParseException e) {
								throw new Exception("Invalid date format");
						}
					}
					catch(java.lang.ClassCastException e){
						rangeValue = rangeEntry.getValue();
					}*/
					builder.key(rangeEntry.getKey()).value(rangeEntry.getValue());
				}
				builder.endObject().endObject().endObject();
			}
			break;
		}
		}
	}
	
	private void getConditionsQuery(String queryOperation, List<String> fields, Object value, JSONBuilder builder) {
	    builder.key("multi_match").object();
	    builder.key("query").value(value).key("operator").value("and").key("type").value("cross_fields");
	    if (null != fields && !fields.isEmpty()) {
	        builder.key("fields").array();
	        for (String str : fields)
	            builder.value(str);
	        builder.endArray();
	    }
	    builder.key("lenient").value(true).endObject();
    }
}
