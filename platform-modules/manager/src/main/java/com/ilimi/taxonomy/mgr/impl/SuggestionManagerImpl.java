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
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.SuggestionCodeConstants;
import com.ilimi.taxonomy.enums.SuggestionConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

/**
 * The Class SuggestionManager provides implementations of the various
 * operations defined in the ISuggestionManager
 * 
 * @author Rashmi N
 * 
 * @see ISuggestionManager
 */
@Component
public class SuggestionManagerImpl extends BaseManager implements ISuggestionManager {

	/** The ControllerUtil */
	private ControllerUtil util = new ControllerUtil();

	/** The Class Logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The ElasticSearchUtil */
	private static ElasticSearchUtil es = new ElasticSearchUtil();

	/** The Object Mapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The SearchProcessor */
	private SearchProcessor processor = new SearchProcessor();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.ISuggestionManager
	 * #saveSuggestion(java.util.Map)
	 */
	@Override
	public Response saveSuggestion(Map<String, Object> request) {
		Response response = new Response();
		try {
			LOGGER.log("Fetching identifier from request");
			String identifier = (String) request.get(SuggestionCodeConstants.objectId.name());
			Node node = util.getNode(SuggestionConstants.GRAPH_ID, identifier);
			if (null != node) {
				LOGGER.log("saving the suggestions to elastic search index" + identifier);
				response = saveSuggestionToEs(request);
				if (checkError(response)) {
					LOGGER.log("Erroneous Response.");
					return response;
				}
			} else {
				throw new ClientException(SuggestionCodeConstants.INVALID_OBJECT_ID.name(),
						"Content_Id doesnt exists | Invalid Content_id");
			}
		} catch (ClientException e) {
			LOGGER.log("Error occured while processing request | Not a valid request", e.getMessage(),e);
			throw e;
		} catch (Exception e) {
			throw new ClientException(SuggestionCodeConstants.INVALID_REQUEST.name(), "Error! Invalid Request");
		}
		LOGGER.log("Returning response from saveSuggestion" + response.getResponseCode());
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.ISuggestionManager
	 * #readSuggestion(java.lang.String,java.util.date)
	 */
	@Override
	public Response readSuggestion(String objectId, String startTime, String endTime) {
		Response response = new Response();
		try {
			LOGGER.log("Checking if received parameters are empty or not" , objectId);
			List<Object> result = getSuggestionByObjectId(objectId, startTime, endTime);
			response.setParams(getSucessStatus());
			response.put(SuggestionCodeConstants.suggestions.name(), result);
			LOGGER.log("Fetching response from elastic search" + result.size());
			if (checkError(response)) {
				LOGGER.log("Erroneous Response.");
				return response;
			}
		} catch (Exception e) {
			LOGGER.log("Exception occured while fetching suggestions for contentId", e.getMessage(), e);
			throw e;
		}
		LOGGER.log("Response received from the readSuggestion" , response.getResponseCode());
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.ISuggestionManager
	 * #approveSuggestion(java.lang.String,java.util.Map)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Response approveSuggestion(String suggestion_id, Map<String, Object> map) {
		Response response = new Response();
		Map<String, Object> data = new HashMap<String, Object>();
		Request request = new Request();
		try {
			Map<String, Object> requestMap = validateRequest(map, SuggestionConstants.EXPECTED_APPROVE_STATUS);
			LOGGER.log("validated request: " + requestMap);
			String requestString = mapper.writeValueAsString(requestMap);

			LOGGER.log("request for suggestion approval" + requestString);
			es.updateDocument(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
					requestString, suggestion_id);
			response.setParams(getSucessStatus());
			response.put(SuggestionCodeConstants.suggestion_id.name(), suggestion_id);
			response.put(SuggestionCodeConstants.message.name(),
					"suggestion accepted successfully! Content Update started successfully");
			if (checkError(response)) {
				LOGGER.log("Erroneous Response.");
				return response;
			}
			String suggestionResponse = es.getDocumentAsStringById(SuggestionConstants.SUGGESTION_INDEX,
					SuggestionConstants.SUGGESTION_INDEX_TYPE, suggestion_id);
			LOGGER.log("Result from getting suggestion from Id" + suggestionResponse);
			Map<String, Object> suggestionObject = mapper.readValue(suggestionResponse, Map.class);
			Map<String, Object> paramsMap = (Map) suggestionObject.get(SuggestionCodeConstants.params.name());
			String contentId = (String) suggestionObject.get(SuggestionCodeConstants.objectId.name());
            LOGGER.log("Content Identifier :" + contentId);
			// making rest call to get content API
			String api_url = PropertiesUtil.getProperty("platform-api-url") + "/v2/content/" + contentId
					+ "?mode=edit";
			
			LOGGER.log("Making HTTP GET call to fetch Content" + api_url);
			String result = HTTPUtil.makeGetRequest(api_url);
			LOGGER.log("result from get HTTP call to get content" + result);
			
			Map<String, Object> resultMap = mapper.readValue(result, Map.class);
			Map<String, Object> responseMap = (Map) resultMap.get(SuggestionCodeConstants.result.name());
			Map<String, Object> contentMap = (Map) responseMap.get(SuggestionCodeConstants.content.name());
			String versionKey = (String) contentMap.get(SuggestionCodeConstants.versionKey.name());
			LOGGER.log("versionKey of content Id" + versionKey);

			// making rest call to content Update
			paramsMap.put(SuggestionCodeConstants.versionKey.name(), versionKey);
			data.put(SuggestionCodeConstants.content.name(), paramsMap);
			request.setRequest(data);
			String url = PropertiesUtil.getProperty("platform-api-url") + "/v2/content/" + contentId;
			
			LOGGER.log("Making HTTP POST call to update content" + url);
			String requestData = mapper.writeValueAsString(request);
			String responseData = HTTPUtil.makePatchRequest(url, requestData);
			LOGGER.log("result from update Content API after updating suggestion metadata" + responseData);
			
		} catch (ClientException e) {
			LOGGER.log("throwing exception received" + e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			LOGGER.log("Server Exception occured while processing request" , e.getMessage(), e);
			throw new ServerException(SuggestionCodeConstants.SERVER_ERROR.name(),
					"Error! Something went wrong while processing", e);
		}
		LOGGER.log("Returning response from approve suggestion" + response.getResponseCode());
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.ISuggestionManager
	 * #rejectSuggestion(java.lang.String, java.util.Map)
	 */
	@Override
	public Response rejectSuggestion(String suggestion_id, Map<String, Object> map) {
		Response response = new Response();
		try {
			Map<String, Object> requestMap = validateRequest(map, SuggestionConstants.EXPECTED_REJECT_STATUS);
			String results = mapper.writeValueAsString(requestMap);
			es.updateDocument(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE, results,
					suggestion_id);
			response.setParams(getSucessStatus());
			response.put("suggestion_id", suggestion_id);
			response.put("message", "suggestion rejected successfully");
			if (checkError(response)) {
				LOGGER.log("Erroneous Response.");
				return response;
			}
		} catch (ClientException e) {
			LOGGER.log("throwing exception received" , e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			LOGGER.log("Server Exception occured while processing request" , e.getMessage(), e);
			throw new ServerException(SuggestionCodeConstants.SERVER_ERROR.name(),
					"Error! Something went wrong while processing", e);
		}

		LOGGER.log("Returning response from rejectSuggestion" + response.getResponseCode());
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.ISuggestionManager
	 * #listSuggestion(java.util.Map)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Response listSuggestion(Map<String, Object> map) {
		Response response = new Response();
		try {
			String status = null;
			String suggestedBy = null;
			String suggestionId = null;
			Map<String, Object> requestMap = (Map) map.get(SuggestionCodeConstants.request.name());
			Map<String, Object> contentReq = (Map) requestMap.get(SuggestionCodeConstants.content.name());
			LOGGER.log("Fetching fields to be retrived from request" + contentReq);
			if (contentReq.containsKey(SuggestionCodeConstants.status.name()))
				status = (String) contentReq.get(SuggestionCodeConstants.status.name());
			if (contentReq.containsKey(SuggestionCodeConstants.suggestedBy.name()))
				suggestedBy = (String) contentReq.get(SuggestionCodeConstants.suggestedBy.name());
			if (contentReq.containsKey(SuggestionCodeConstants.suggestion_id.name()))
				suggestionId = (String) contentReq.get(SuggestionCodeConstants.suggestion_id.name());

			LOGGER.log("calling getSuggestion method to get suggestions based on search criteria" + status
					+ suggestedBy + suggestionId);
			List<Object> list = getSuggestionsList(status, suggestedBy, suggestionId);

			LOGGER.log("Result from suggestion list API" + list);
			response.setParams(getSucessStatus());
			response.put("suggestions", list);
			if (checkError(response)) {
				LOGGER.log("Erroneous Response.");
				return response;
			}
		} catch (ClientException e) {
			LOGGER.log("throwing exception received" , e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			LOGGER.log("Server Exception occured while processing request" , e.getMessage(), e);
			throw new ServerException(SuggestionCodeConstants.SERVER_ERROR.name(),
					"Error! Something went wrong while processing", e);
		}
		LOGGER.log("Returning response from list suggestion" + response.getResponseCode());
		return response;
	}

	/**
	 * This methods holds logic to save suggestion to elastic search index
	 * 
	 * @param entity_map
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	private Response saveSuggestionToEs(Map<String, Object> entity_map) throws IOException {
		Response response = new Response();
		LOGGER.log("creating suggestion index in elastic search" + SuggestionConstants.SUGGESTION_INDEX);
		createIndex();
		LOGGER.log("Adding document to suggestion index" , entity_map);
		String identifier = addDocument(entity_map);

		LOGGER.log("Checking if suggestion_if is returned from response" + identifier);
		if (StringUtils.isNotBlank(identifier)) {
			response = setResponse(response, identifier);
			LOGGER.log("returning response from save suggestion" , response);
			return response;
		}
		return null;
	}

	/**
	 * This methods holds logic to create index for suggestions in elastic
	 * search
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	private static void createIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \"" + SuggestionConstants.SUGGESTION_INDEX
				+ "\",     \"type\": \"" + SuggestionConstants.SUGGESTION_INDEX_TYPE
				+ "\",     \"analysis\": {       \"analyzer\": {         \"sg_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"sg_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \"" + SuggestionConstants.SUGGESTION_INDEX_TYPE
				+ "\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"sg_index_analyzer\",            \"search_analyzer\": \"sg_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"sg_index_analyzer\",        \"search_analyzer\": \"sg_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		LOGGER.log("Creating Suggestion Index : " + SuggestionConstants.SUGGESTION_INDEX);
		es.addIndex(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE, settings,
				mappings);
	}

	/**
	 * This methods holds logic to add document to suggestions index in elastic
	 * search
	 * 
	 * @return SuggestionId
	 * 
	 * @throws IOException
	 */
	private static String addDocument(Map<String, Object> request) throws IOException {
		String suggestionId = "sg_" + Identifier.getUniqueIdFromTimestamp();
		String document = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		if (StringUtils.isNoneBlank(suggestionId)) {
			request.put(SuggestionCodeConstants.suggestion_id.name(), suggestionId);
			LOGGER.log("Checking if document is empty : " + request);

			if (!request.isEmpty()) {
				request.put(SuggestionCodeConstants.createdOn.name(), df.format(new Date()));
				request.put(SuggestionCodeConstants.status.name(), "new");
				document = mapper.writeValueAsString(request);
				LOGGER.log("converting request map to string : " + document);
			}
			if (StringUtils.isNotBlank(document)) {
				es.addDocumentWithId(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
						suggestionId, document);
				LOGGER.log("Adding document to Suggetion Index : " + document);
			}
		}
		LOGGER.log("Returning suggestionId as response to saved suggestion" + suggestionId);
		return suggestionId;
	}

	/**
	 * This method holds logic to setResponse
	 * 
	 * @param response
	 *            The response
	 * 
	 * @param suggestionId
	 *            The suggestionId
	 * 
	 * @return The response
	 */
	private Response setResponse(Response response, String suggestionId) {
		LOGGER.log("Setting response" + suggestionId);
		response.setParams(getSucessStatus());
		response.getResult().put("suggestion_id", suggestionId);
		response.setResponseCode(response.getResponseCode());
		return response;
	}

	/**
	 * This method holds logic to getSuggestionById from elastic search based on
	 * search criteria
	 * 
	 * @param objectId
	 *            The objectId
	 * 
	 * @param start_date
	 *            The startDate
	 * 
	 * @param end_date
	 *            The endDate
	 * 
	 * @return The List of suggestions for given objectId
	 */
	private List<Object> getSuggestionByObjectId(String objectId, String start_date, String end_date) {
		SearchDTO search = new SearchDTO();
		search.setProperties(setSearchFilters(objectId, start_date, end_date, null, null, null));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
		search.setSortBy(sortBy);
		LOGGER.log("setting search criteria to fetch records from ES" + search);
		List<Object> suggestionResult = search(search);
		LOGGER.log("list of fields returned from ES based on search query" + suggestionResult);
		return suggestionResult;
	}

	/**
	 * This method holds logic to get Suggestions based on search criteria
	 * 
	 * @param status
	 *            The status
	 * 
	 * @param suggestedBy
	 *            The suggestedBy
	 * 
	 * @param suggestionId
	 *            The suggestionId
	 * 
	 * @return List of suggestions
	 */
	private List<Object> getSuggestionsList(String status, String suggestedBy, String suggestionId) {
		SearchDTO search = new SearchDTO();
		search.setProperties(setSearchFilters(null, null, null, status, suggestedBy, suggestionId));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
		search.setSortBy(sortBy);
		LOGGER.log("setting search criteria to fetch records from ES" + search);
		List<Object> suggestionResult = search(search);
		LOGGER.log("list of fields returned from ES based on search query" + suggestionResult);
		return suggestionResult;
	}

	/**
	 * This method holds logic to set Search criteria
	 * 
	 * @param objectId
	 *            The objectId
	 * 
	 * @param start_date
	 *            The startDate
	 * 
	 * @param end_date
	 *            The endDate
	 * 
	 * @param status
	 *            The status
	 * 
	 * @param suggestedBy
	 *            The suggestedBy
	 * 
	 * @param suggestion_id
	 *            The suggestionId
	 * 
	 * @return List of suggestions
	 */
	@SuppressWarnings("rawtypes")
	private List<Map> setSearchFilters(String objectId, String start_date, String end_date, String status,
			String suggestedBy, String suggestion_id) {
		List<Map> properties = new ArrayList<Map>();

		LOGGER.log("setting search criteria for start_date");
		if (StringUtils.isNotBlank(start_date)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE);
			property.put("propertyName", GraphDACParams.createdOn.name());
			Map<String, Object> range_map = new HashMap<String, Object>();
			range_map.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE, start_date);
			property.put("values", range_map);
			properties.add(property);
		}
		LOGGER.log("setting search criteria for end_date");
		if (StringUtils.isNotBlank(end_date)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE);
			property.put("propertyName", GraphDACParams.createdOn.name());
			Map<String, Object> range_map = new HashMap<String, Object>();
			range_map.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE, end_date);
			property.put("values", range_map);
			properties.add(property);
		}
		LOGGER.log("setting search criteria for objectId");
		if (StringUtils.isNotBlank(objectId)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "objectId");
			property.put("values", Arrays.asList(objectId));
			properties.add(property);
		}
		LOGGER.log("setting search criteria for status");
		if (StringUtils.isNotBlank(status)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "status");
			property.put("values", Arrays.asList(status));
			properties.add(property);
		}
		LOGGER.log("setting search criteria for suggestedBy");
		if (StringUtils.isNotBlank(suggestedBy)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "suggestedBy");
			property.put("values", Arrays.asList(suggestedBy));
			properties.add(property);
		}
		LOGGER.log("setting search criteria for suggestion_id");
		if (StringUtils.isNotBlank(suggestion_id)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "suggestion_id");
			property.put("values", Arrays.asList(suggestion_id));
			properties.add(property);
		}
		LOGGER.log("returning the search filters" + properties);
		return properties;
	}

	/**
	 * This method holds logic to call search processor to search suggestions
	 * based on criteria
	 * 
	 * @param search
	 *            The searchDto
	 * 
	 * @return List of suggestions
	 */
	public List<Object> search(SearchDTO search) {
		List<Object> result = new ArrayList<Object>();
		try {
			LOGGER.log("sending search request to search processor" + search);
			result = (List<Object>) processor.processSearchAuditHistory(search, false,
					SuggestionConstants.SUGGESTION_INDEX);
			LOGGER.log("result from search processor" + result);
		} catch (Exception e) {
			LOGGER.log("error while processing the search request", e.getMessage(), e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This method holds logic to validate request
	 * 
	 * @param requestMap
	 *            The requestMap
	 * 
	 * @param expectedStatus
	 *            The expectedStatus
	 * 
	 * @return requestMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> validateRequest(Map<String, Object> requestMap, String expectedStatus) {
		Map<String, Object> contentMap = new HashMap<String, Object>();
		try {
			if (null != requestMap && !requestMap.isEmpty()) {
				Map<String, Object> requestObj = (Map) requestMap.get(SuggestionCodeConstants.request.name());
				contentMap = (Map) requestObj.get(SuggestionCodeConstants.content.name());
				String status = (String) contentMap.get(SuggestionCodeConstants.status.name());
				LOGGER.log("Status check for validations" + status + expectedStatus);
				if (StringUtils.isNotBlank(status) && StringUtils.equalsIgnoreCase(status, expectedStatus)) {
					return contentMap;
				} else {
					throw new ClientException(SuggestionCodeConstants.MISSING_STATUS.name(), "Error! Invalid status");
				}
			}
		} catch (ClientException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log("ClientException : invalidRequest");
			throw new ClientException(SuggestionCodeConstants.INVALID_REQUEST.name(), "Error! Invalid Request", e);
		}
		return null;
	}
}
