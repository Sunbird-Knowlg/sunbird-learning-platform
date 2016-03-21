package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

import io.searchbox.core.SearchResult;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class SearchProcessor {

	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Object> processSearch(SearchDTO searchDTO) throws IOException {
		List<Map> conditionsSetOne = new ArrayList<Map>();
		List<Map> conditionsSetArithmetic = new ArrayList<Map>();
		List<Map> conditionsSetMustNot = new ArrayList<Map>();
		Map<String, List> conditionsMap = new HashMap<String, List>();
		conditionsMap.put("Text", conditionsSetOne);
		conditionsMap.put("Arithmetic", conditionsSetArithmetic);
		conditionsMap.put("Not", conditionsSetMustNot);

		List<Map> properties = searchDTO.getProperties();

		String totalOperation = searchDTO.getOperation();
		for (Map<String, Object> property : properties) {
			String propertyName = (String) property.get("propertyName");
			if(propertyName.equals("*")){
				propertyName = "all_fields";
			}
			String operation = (String) property.get("operation");
			List<Object> values = (List<Object>) property.get("values");
			String queryOperation = null;
			String conditionSet = null;
			switch (operation) {
			case CompositeSearchConstants.SEARCH_OPERATION_EQUAL: {
				queryOperation = "equal";
				conditionSet = "Text";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL: {
				queryOperation = "equal";
				conditionSet = "Not";
				break;
			}

			case CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH:{
				queryOperation = "endsWith";
				conditionSet = "Text";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LIKE:
			case CompositeSearchConstants.SEARCH_OPERATION_CONTAINS: {
				queryOperation = "like";
				conditionSet = "Text";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_LIKE: {
				queryOperation = "like";
				conditionSet = "Not";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH: {
				queryOperation = "prefix";
				conditionSet = "Text";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_EXISTS: {
				queryOperation = "exists";
				conditionSet = "Text";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_NOT_EXISTS: {
				queryOperation = "exists";
				conditionSet = "Not";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN: {
				queryOperation = ">";
				conditionSet = "Arithmetic";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				queryOperation = ">=";
				conditionSet = "Arithmetic";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN: {
				queryOperation = "<";
				conditionSet = "Arithmetic";
				break;
			}
			case CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				queryOperation = "<=";
				conditionSet = "Arithmetic";
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
			} else {
				condition.put("operation", queryOperation);
				condition.put("fieldName", propertyName);
				condition.put("value", values.get(0));
			}
			conditionsMap.get(conditionSet).add(condition);
		}
		String query = makeElasticSearchQuery(conditionsMap, totalOperation);
		elasticSearchUtil.setDefaultResultLimit(searchDTO.getLimit());
		SearchResult searchResult = elasticSearchUtil.search(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, query);
		List<Object> result = elasticSearchUtil.getDocumentsFromSearchResult(searchResult, Map.class);
		System.out.println("True");
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String makeElasticSearchQuery(Map<String, List> conditionsMap, String totalOperation) {
		JSONBuilder builder = new JSONStringer();
		builder.object().key("query").object().key("filtered").object().key("query").object().key("bool").object();

		List<Map> textConditions = conditionsMap.get("Text");
		if (textConditions != null && !textConditions.isEmpty()) {
			String allOperation = "should";
			if (totalOperation == "AND") {
				allOperation = "must";
			}
			builder.key(allOperation).array();
			for (Map textCondition : textConditions) {
				String conditionOperation = (String) textCondition.get("operation");
				if (conditionOperation.equalsIgnoreCase("bool")) {
					String operand = (String) textCondition.get("operand");
					builder.object().key("bool").object();
					builder.key(operand).array();
					List<Map> subConditions = (List<Map>) textCondition.get("subConditions");
					for (Map subCondition : subConditions) {
						builder.object();
						String queryOperation = (String) subCondition.get("operation");
						String fieldName = (String) subCondition.get("fieldName");
						String value = (String) subCondition.get("value");
						switch (queryOperation) {
						case "equal": {
							builder.key("match").object().key(fieldName + ".raw").value(value).endObject();
							break;
						}
						case "like": {
							builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value+"*")
							.endObject().endObject();
							break;
						}
						case "prefix": {
							builder.key("query").object().key("prefix").object().key(fieldName + ".raw").value(value)
									.endObject().endObject();
							break;
						}
						case "exists": {
							builder.key("exists").object().key("field").value(value).endObject();
							break;
						}
						case "endsWith": {
							builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value)
							.endObject().endObject();
							break;
						}
						}
						builder.endObject();
					}
					builder.endArray();
					builder.endObject().endObject();
				} else {
					builder.object();
					String queryOperation = (String) textCondition.get("operation");
					String fieldName = (String) textCondition.get("fieldName");
					Object value = (Object) textCondition.get("value");
					switch (queryOperation) {
					case "equal": {
						builder.key("match").object().key(fieldName + ".raw").value(value).endObject();
						break;
					}
					case "like": {
						builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value+"*")
						.endObject().endObject();
						break;
					}
					case "prefix": {
						builder.key("query").object().key("prefix").object().key(fieldName + ".raw").value(value)
								.endObject().endObject();
						break;
					}
					case "exists": {
						builder.key("exists").object().key("field").value(value).endObject();
						break;
					}
					case "endsWith": {
						builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value)
						.endObject().endObject();
						break;
					}
					}
					builder.endObject();

				}
			}
			builder.endArray();
		}

		List<Map> arithmeticConditions = conditionsMap.get("Arithmetic");
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

		List<Map> notConditions = conditionsMap.get("Not");
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
						String value = (String) subCondition.get("value");
						switch (queryOperation) {
						case "equal": {
							builder.key("match").object().key(fieldName + ".raw").value(value).endObject();
							break;
						}
						case "like": {
							builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value+"*")
							.endObject().endObject();
							break;
						}
						case "prefix": {
							builder.key("query").object().key("prefix").object().key(fieldName + ".raw").value(value)
									.endObject().endObject();
							break;
						}
						case "exists": {
							builder.key("exists").object().key("field").value(value).endObject();
							break;
						}
						case "endsWith": {
							builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value)
							.endObject().endObject();
							break;
						}
						}
						builder.endObject();
					}
					builder.endArray();
					builder.endObject().endObject();
				} else {
					builder.object();
					String queryOperation = (String) notCondition.get("operation");
					String fieldName = (String) notCondition.get("fieldName");
					String value = (String) notCondition.get("value");
					switch (queryOperation) {
					case "equal": {
						builder.key("match").object().key(fieldName + ".raw").value(value).endObject();
						break;
					}
					case "like": {
						builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value+"*")
						.endObject().endObject();
						break;
					}
					case "prefix": {
						builder.key("query").object().key("prefix").object().key(fieldName + ".raw").value(value)
								.endObject().endObject();
						break;
					}
					case "exists": {
						builder.key("exists").object().key("field").value(value).endObject();
						break;
					}
					case "endsWith": {
						builder.key("query").object().key("wildcard").object().key(fieldName + ".raw").value("*"+value)
						.endObject().endObject();
						break;
					}
					}
					builder.endObject();
				}
			}
			builder.endArray();
		}
		builder.endObject().endObject().endObject().endObject().endObject();
		return builder.toString();
	}
}
