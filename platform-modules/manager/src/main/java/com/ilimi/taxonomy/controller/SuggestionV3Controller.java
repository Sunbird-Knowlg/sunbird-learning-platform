package com.ilimi.taxonomy.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.taxonomy.enums.SuggestionErrorCodeConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

@Controller
@RequestMapping("/v3/suggestions")
public class SuggestionV3Controller extends BaseController {

	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(SuggestionV3Controller.class.getName());

	@Autowired
	private ISuggestionManager suggestionManager;

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> map) {
		String apiId = "content.suggestions.create";
		Map<String,Object>  request = getSuggestionRequest(map);
		LOGGER.info("Create | Suggestions: " + " | Request: " + request);
		try {
			Response response = suggestionManager.saveSuggestion(request);
			LOGGER.info("Create | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String object_id,
			@RequestParam(name = "start", required = false) String startTime,
			@RequestParam(name = "end", required = false) String endTime,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.suggestions.read";
		LOGGER.info("Get | Suggestions: " + " | Request: " + object_id);
		try {
			Response response = suggestionManager.readSuggestion(object_id, startTime, endTime);
			LOGGER.info("Create | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	@RequestMapping(value = "/approve/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> approve(@RequestBody Map<String, Object> map,
			@PathVariable(value = "id") String suggestion_id,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.suggestions.approve";
		LOGGER.info("Get | Suggestions: " + " | Request: " + suggestion_id);
		try {
			if(StringUtils.isBlank(suggestion_id)){
				throw new ClientException(SuggestionErrorCodeConstants.Missing_object_id.name(), "Error! Invalid or Missing Object_Id");
			}
			Response response = suggestionManager.approveSuggestion(suggestion_id, map);
			LOGGER.info("Create | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	@RequestMapping(value = "/reject/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> reject(@RequestBody Map<String, Object> map,
			@PathVariable(value = "id") String suggestion_id,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.suggestions.reject";
		LOGGER.info("Get | Suggestions: " + " | Request: " + suggestion_id);
		try {
			if(StringUtils.isBlank(suggestion_id)){
				throw new ClientException(SuggestionErrorCodeConstants.Missing_object_id.name(), "Error! Invalid or Missing Object_Id");
			}
			Response response = suggestionManager.rejectSuggestion(suggestion_id, map);
			LOGGER.info("Create | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> list(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.suggestions.reject";
		LOGGER.info("Get | Suggestions: " + " | Request: " + map);
		try {
			Response response = suggestionManager.listSuggestion(map);
			LOGGER.info("Create | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> getSuggestionRequest(Map<String, Object> requestMap) {
		Request request = getRequest(requestMap);
		Map<String, Object> request_map = request.getRequest();
		if (null != request_map && !request_map.isEmpty()) {
			Map<String,Object> map = (Map)request_map.get("content");
			if (null == map.get("objectId")) {
				throw new ClientException(SuggestionErrorCodeConstants.Missing_object_id.name(),
						"Invalid Request | Missing Content_ID parameter");
			}
			if (null == map.get("objectType")) {
				throw new ClientException(SuggestionErrorCodeConstants.Missing_objectType.name(),
						"Invalid Request | Missing ObjectType parameter");
			}
			if (null == map.get("command")) {
				throw new ClientException(SuggestionErrorCodeConstants.Missing_command.name(),
						"Invalid Request | Missing Command parameter");
			}
			if (null == map.get("params")) {
				throw new ClientException(SuggestionErrorCodeConstants.Missing_params.name(),
						"Invalid Request | Missing params parameter");
			}
			if (null == map.get("suggestedBy")) {
				throw new ClientException(SuggestionErrorCodeConstants.Missing_suggestedBy.name(),
						"Invalid Request | Missing SuggestedBy parameter");
			} else {
				return map;
			}
		}
		return null;
	}
}
