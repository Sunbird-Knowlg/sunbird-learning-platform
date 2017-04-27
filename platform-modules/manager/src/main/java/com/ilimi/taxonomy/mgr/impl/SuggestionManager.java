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
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.SuggestionConstants;
import com.ilimi.taxonomy.enums.SuggestionErrorCodeConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

@Component
public class SuggestionManager extends BaseManager implements ISuggestionManager {

	/** The ControllerUtil */
	private ControllerUtil util = new ControllerUtil();

	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(SuggestionManager.class.getName());

	/** The ElasticSearchUtil */
	private static ElasticSearchUtil es = new ElasticSearchUtil();

	/** The Object Mapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The SearchProcessor */
	private SearchProcessor processor = new SearchProcessor();

	/** The status List */
	private static List<String> statusList = new ArrayList<String>();

	static {
		statusList.add("approve");
		statusList.add("reject");
	}

	@Override
	public Response saveSuggestion(Map<String, Object> request) {
		Response response = null;
		try {
			LOGGER.info("Fetching identifier from request");
			String identifier = (String) request.get("objectId");
			Node node = util.getNode(SuggestionConstants.GRAPH_ID, identifier);
			if (StringUtils.equalsIgnoreCase(identifier, node.getIdentifier())) {
				LOGGER.info("saving the suggestions to elsatic search index" + identifier);
				response = saveSuggestionToEs(request);
				if (checkError(response)) {
					LOGGER.info("Erroneous Response.");
					return response;
				}
			} else {
				throw new ClientException(SuggestionErrorCodeConstants.Invalid_object_id.name(),
						"Content_Id doesnt exists | Invalid Content_id");
			}
		} catch (Exception e) {
			LOGGER.error("Error occured while processing request | Not a valid request", e);
			throw new ClientException(SuggestionErrorCodeConstants.Invalid_request.name(),
					"Error occured while processing request | Not a valid request");
		}
		return response;
	}

	@Override
	public Response readSuggestion(String objectId, String startTime, String endTime) {
		Response response = new Response();
		try {
			LOGGER.debug("Checking if received parameters are empty or not" + objectId);
			List<Object> result = getSuggestionByObjectId(objectId, startTime, endTime);
			response.put("suggestions", result);
			if (checkError(response)) {
				LOGGER.info("Erroneous Response.");
				return response;
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while fetching suggestions for contentId", e);
			throw e;
		}
		LOGGER.info("Response received from the search as a result" + response);
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Response approveSuggestion(String suggestion_id, Map<String, Object> map) {
		Response response = new Response();
		if (StringUtils.isBlank(suggestion_id)) {
			throw new ClientException(SuggestionErrorCodeConstants.Missing_suggestion_id.name(),
					"Error! Invalid | Missing suggestion_id");
		}
		try {
			Map<String, Object> requestMap = validateRequest(map);
			if (!requestMap.get("status").equals("approve")) {
				throw new ClientException(SuggestionErrorCodeConstants.Invalid_request.name(),
						"Error! metadata status should be 'approve' to approve any suggestion");
			} else {
				String results = mapper.writeValueAsString(requestMap);
				es.updateDocument(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
						results, suggestion_id);
				response.put("suggestion_id", suggestion_id);
				response.put("message", "suggestion accepted successfully! Content Update started successfully");
				if (checkError(response)) {
					LOGGER.info("Erroneous Response.");
					return response;
				}
				String suggestionResponse = es.getDocumentAsStringById(SuggestionConstants.SUGGESTION_INDEX,
						SuggestionConstants.SUGGESTION_INDEX_TYPE, suggestion_id);
				Map<String, Object> suggestionObject = mapper.readValue(suggestionResponse, Map.class);
				Map<String, Object> paramsMap = (Map) suggestionObject.get("params");
				String contentId = (String) suggestionObject.get("objectId");
				String api_url = PropertiesUtil.getProperty("ekstepPlatformURI") + "/v2/content/" + contentId;
				Node node = util.getNode(SuggestionConstants.GRAPH_ID, contentId);
				Request request = new Request();
				Map<String,Object> data = new HashMap<String,Object>();
				paramsMap.put("versionKey", node.getMetadata().get("versionKey"));
				data.put("content", paramsMap);
				request.setRequest(data);
				String s = mapper.writeValueAsString(request);
				String result = HTTPUtil.makePatchRequest(api_url, s);
				System.out.println(result);
//				Node node = util.getNode(SuggestionConstants.GRAPH_ID, contentId);
//				node.setGraphId(GraphDACParams.graph_id.name());
//				node.setIdentifier(contentId);
//				for(Map.Entry<String, Object> entry : paramsMap.entrySet()){
//					
//				}
//				node.setMetadata(paramsMap);
//				response = util.updateNode(node);
				if (checkError(response)) {
					LOGGER.info("Erroneous Response.");
					return response;
				}
			}
		} catch (Exception e) {
			throw new ServerException(SuggestionErrorCodeConstants.server_error.name(),
					"Error! Something went wrong while processing", e);
		}
		return response;
	}

	@Override
	public Response rejectSuggestion(String suggestion_id, Map<String, Object> map) {
		Response response = new Response();
		if (StringUtils.isBlank(suggestion_id)) {
			throw new ClientException(SuggestionErrorCodeConstants.Missing_suggestion_id.name(),
					"Error! Invalid | Missing suggestion_id");
		}
		try {
			Map<String, Object> requestMap = validateRequest(map);
			if (!requestMap.get("status").equals("reject")) {
				throw new ClientException(SuggestionErrorCodeConstants.Invalid_request.name(),
						"Error! metadata status should be 'reject' to reject any suggestion");
			} else {
				String results = mapper.writeValueAsString(requestMap);
				es.updateDocument(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
						results, suggestion_id);
				response.put("suggestion_id", suggestion_id);
				response.put("message", "suggestion rejected successfully");
				if (checkError(response)) {
					LOGGER.info("Erroneous Response.");
					return response;
				}
			}
		} catch (Exception e) {
			throw new ServerException(SuggestionErrorCodeConstants.server_error.name(),
					"Error! Something went wrong while processing", e);
		}
		return response;

	}

	@Override
	public Response listSuggestion(Map<String, Object> map) {
		Response response = new Response();
		Request request = new Request();
		try {
			request.put("request", map);
//			response = util.getSearchDto(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public Response saveSuggestionToEs(Map<String, Object> entity_map) throws IOException {
		Response response = new Response();
		LOGGER.info("creating suggestion index in elastic search" + SuggestionConstants.SUGGESTION_INDEX);
		createIndex();
		LOGGER.info("Adding dowcument to suggestion index" + entity_map);
		String identifier = addDocument(entity_map);

		LOGGER.info("Checking if suggestion_if is returned from response" + identifier);
		if (StringUtils.isNotBlank(identifier)) {
			response = setResponse(response, identifier);
			LOGGER.info("returning response from save suggestion" + response);
			return response;
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
				request.put("createdOn", df.format(new Date()));
				request.put("status", "new");
				document = mapper.writeValueAsString(request);
				LOGGER.info("converting request map to string : " + document);
			}
			if (StringUtils.isNotBlank(document)) {
				es.addDocumentWithId(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
						suggestionId, document);
				LOGGER.info("Adding document to Suggetion Index : " + document);
			}
		}
		return suggestionId;
	}

	public Response setResponse(Response response, String suggestionId) {
		LOGGER.info("Setting response" + suggestionId);
		response.setParams(response.getParams());
		response.getResult().put("suggestion_id", suggestionId);
		response.setResponseCode(response.getResponseCode());
		return response;
	}

	protected List<Object> getSuggestionByObjectId(String objectId, String start_date, String end_date) {
		SearchDTO search = new SearchDTO();
		search.setProperties(setSearchFilters(objectId, start_date, end_date));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
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

	public List<Object> search(SearchDTO search) {
		List<Object> result = new ArrayList<Object>();
		try {
			LOGGER.info("sending search request to search processor" + search);
			result = (List<Object>) processor.processSearchAuditHistory(search, false,
					SuggestionConstants.SUGGESTION_INDEX);
			LOGGER.info("result from search processor" + result);
		} catch (Exception e) {
			LOGGER.error("error while processing the search request", e);
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> validateRequest(Map<String, Object> map) {
		Map<String, Object> content = new HashMap<String, Object>();
		try {
			if (null != map && !map.isEmpty()) {
				Map<String, Object> requestObj = (Map) map.get("request");
				content = (Map) requestObj.get("content");
				String status = (String) content.get("status");
				if (StringUtils.isNotBlank(status) && statusList.contains(status)) {
					return content;
				}
			}
		} catch (Exception e) {
			throw new ClientException(SuggestionErrorCodeConstants.Invalid_request.name(), "Error! Invalid Request");
		}
		return null;
	}
}
