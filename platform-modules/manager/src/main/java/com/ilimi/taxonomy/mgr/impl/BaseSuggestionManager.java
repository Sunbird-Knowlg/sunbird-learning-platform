package com.ilimi.taxonomy.mgr.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.taxonomy.enums.SuggestionConstants;

public class BaseSuggestionManager {
	
	/** The ElasticSearchUtil */
	private static ElasticSearchUtil es = new ElasticSearchUtil();

	/** The SearchProcessor */
	private SearchProcessor processor = new SearchProcessor();
	
	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(SuggestionManager.class.getName());
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public String saveSuggestionToEs(Map<String, Object> entity_map) throws IOException {
		createIndex();
		String identifier = addDocument(entity_map);
		if(StringUtils.isNotBlank(identifier)){
			return identifier;
		}
		return null;
	}

	public static void createIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \"" + SuggestionConstants.SUGGESTION_INDEX
				+ "\",     \"type\": \"" + SuggestionConstants.SUGGESTION_INDEX_TYPE
				+ "\",     \"analysis\": {       \"analyzer\": {         \"sg_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"sg_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \"" + SuggestionConstants.SUGGESTION_INDEX_TYPE
				+ "\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"sg_index_analyzer\",            \"search_analyzer\": \"sg_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"sg_index_analyzer\",        \"search_analyzer\": \"sg_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		LOGGER.info("Creating Suggestion Index : " + SuggestionConstants.SUGGESTION_INDEX);
		es.addIndex(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE, settings,
				mappings);
	}

	public static String addDocument(Map<String, Object> request) throws IOException {
		String suggestionId = "sg_" + Identifier.getUniqueIdFromTimestamp();
		String document = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		if (StringUtils.isNoneBlank(suggestionId)) {
			request.put("suggestionId", suggestionId);
			LOGGER.info("Checking if document is empty : " + request);
			
			if (!request.isEmpty()) {
				request.put("createdOn",df.format(new Date()));
				document = mapper.writeValueAsString(request);
				LOGGER.info("converting request map to string : " + document);
			}
			if (StringUtils.isNotBlank(document)) {
				es.addDocument(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
						document);
				LOGGER.info("Adding document to Suggetion Index : " + document);
			}
		}
		return suggestionId;
	}
	
	public Response setResponse(Response response, String suggestionId) {
		response.setParams(response.getParams());
		response.getResult().put("suggestion_id", suggestionId);
		response.setResponseCode(response.getResponseCode());
		return response;
	}

	protected List<Object> getSuggestionByObjectId(Request request) {
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		String start_date = (String) request.get(CommonDACParams.start_date.name());
		String end_date = (String) request.get(CommonDACParams.end_date.name());
		SearchDTO search = new SearchDTO();
		search.setFields(setSearchCriteria());
		search.setProperties(setSearchFilters( objectId, start_date, end_date));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
		//sortBy.put("operation", "desc");
		search.setSortBy(sortBy);
		LOGGER.info("setting search criteria to fetch audit records from ES" + search);
		List<Object> suggestionResult = search(search);
		LOGGER.info("list of fields returned from ES based on search query" + suggestionResult);
		return suggestionResult;
	}
	
	@SuppressWarnings("rawtypes")
	public List<Map> setSearchFilters(String objectId, String start_date, String end_date) {
		List<Map> properties = new ArrayList<Map>();

		if (StringUtils.isNotBlank(start_date)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE);
			property.put("propertyName", GraphDACParams.createdOn.name());
			Map<String, Object> range_map = new HashMap<String, Object>();
			range_map.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE, start_date);
			property.put("values", range_map);
			properties.add(property);
		}
		if (StringUtils.isNotBlank(end_date)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE);
			property.put("propertyName", GraphDACParams.createdOn.name());
			Map<String, Object> range_map = new HashMap<String, Object>();
			range_map.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE, end_date);
			property.put("values", range_map);
			properties.add(property);
		}
		if (StringUtils.isNotBlank(objectId)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "objectId");
			property.put("values", Arrays.asList(objectId));
			properties.add(property);
		}
		LOGGER.info("returning the search filters" + properties);
		return properties;
	}
	public List<String> setSearchCriteria() {
		List<String> fields = new ArrayList<String>();
		fields.add("objectId");
		fields.add("objectType");
		fields.add("command");
		fields.add("suggestedBy");
		fields.add("params");
		fields.add("suggestionId");
		fields.add("createdOn");
		LOGGER.info("returning the search criteria fields" + fields);
		return fields;
	}
	
	public List<Object> search(SearchDTO search) {
		List<Object>  result= new ArrayList<Object>();
			try {
				LOGGER.info("sending search request to search processor" + search);
				result = (List<Object>) processor.processSearchAuditHistory(search, false, SuggestionConstants.SUGGESTION_INDEX);
				LOGGER.info("result from search processor" + result);
			} catch (Exception e) {
				LOGGER.error("error while processing the search request", e);
				e.printStackTrace();
			}
		return result;
	}
}
