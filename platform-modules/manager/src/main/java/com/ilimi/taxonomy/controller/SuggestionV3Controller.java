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
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.taxonomy.enums.SuggestionCodeConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

/**
 * The Class SuggestionV3Controller is main entry point for all,
 * curation workflow operations. This provides APIs for
 * object suggestions and related operations
 * 
 * * All the Methods are backed by their corresponding manager classes, which have the
 * actual logic to communicate with the middleware and core level APIs.
 * 
 * @author Rashmi N
 * 
 */

@Controller
@RequestMapping("/v3/suggestions")
public class SuggestionV3Controller extends BaseController {

	/** The Class Logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Autowired
	private ISuggestionManager suggestionManager;

	/**
	 * This method holds all operations related to saving suggestions for a 
	 * given object
	 * 
	 * It creates suggestion object for a given objectId and saves it to
	 * elastic search
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.suggestions.create";
		try {
			Map<String,Object>  request = validateSuggestionRequest(map);
			LOGGER.log("Create | Suggestions: " + " | Request: " + request);
			Response response = suggestionManager.saveSuggestion(request);
			LOGGER.log("Create | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method holds all operations related to get suggestions for a 
	 * given object identifier
	 * 
	 * It gets suggestion object for a given objectId based on createdOn date
	 * elastic search
	 * 
	 * @param
	 * 		objectId
	 * 
	 * @requestParam
	 * 		startDate
	 * 
	 * @requestParam
	 *     endDate
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String object_id,
			@RequestParam(name = "start", required = false) String startTime,
			@RequestParam(name = "end", required = false) String endTime,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.content.suggestions.read";
		LOGGER.log("Get | Suggestions: " + " | Request: " + object_id);
		try {
			Response response = suggestionManager.readSuggestion(object_id, startTime, endTime);
			LOGGER.log("Create | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	/**
	 * This method holds all operations related to approving suggestions for a 
	 * given object identifier
	 * 
	 * It approves suggestion for a given objectId and updates it on the 
	 * given object Node, updates the suggestion object with approved status
	 * 
	 * @param
	 *    suggestion_id
	 */
	@RequestMapping(value = "/approve/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> approve(@RequestBody Map<String, Object> map,
			@PathVariable(value = "id") String suggestion_id,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.content.suggestions.approve";
		LOGGER.log("Get | Suggestions: " + " | Request: " + suggestion_id);
		try {
			if(StringUtils.isBlank(suggestion_id)){
				throw new ClientException(SuggestionCodeConstants.MISSING_OBJECT_ID.name(), "Error! Invalid or Missing Object_Id");
			}
			Response response = suggestionManager.approveSuggestion(suggestion_id, map);
			LOGGER.log("Create | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	/**
	 * This method holds all operations related to rejecting suggestions for a 
	 * given object identifier
	 * 
	 * It rejects suggestion for a given objectId and updates the suggestion object with 
	 * rejection status
	 * 
	 * @param
	 *    suggestion_id
	 */
	@RequestMapping(value = "/reject/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> reject(@RequestBody Map<String, Object> map,
			@PathVariable(value = "id") String suggestion_id,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.content.suggestions.reject";
		LOGGER.log("Get | Suggestions: " + " | Request: " + suggestion_id);
		try {
			Response response = suggestionManager.rejectSuggestion(suggestion_id, map);
			LOGGER.log("Create | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	/**
	 * This method holds all operations related to listing suggestions based
	 * on search criteria
	 * 
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> list(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.content.suggestions.list";
		LOGGER.log("Get | Suggestions: " + " | Request: " + map);
		try {
			Response response = suggestionManager.listSuggestion(map);
			LOGGER.log("Create | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}	
	
	/**
	 * This method validates if the incoming request is valid or not
	 * 
	 * @param
	 *    requestMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> validateSuggestionRequest(Map<String, Object> requestMap) {
		Request request = getRequest(requestMap);
		Map<String, Object> request_map = request.getRequest();
		if (null != request_map && !request_map.isEmpty()) {
			Map<String,Object> map = (Map)request_map.get(SuggestionCodeConstants.content.name());
			
			String objectId = (String) map.get(SuggestionCodeConstants.objectId.name());
			if (StringUtils.isBlank(objectId) || !map.containsKey(SuggestionCodeConstants.objectId.name())) {
				throw new ClientException(SuggestionCodeConstants.MISSING_OBJECT_ID.name(),
						"Invalid Request | Missing ObjectId parameter");
			}
			String objectType = (String) map.get(SuggestionCodeConstants.objectType.name());
			if (StringUtils.isBlank(objectType) || !map.containsKey(SuggestionCodeConstants.objectType.name())) {
				throw new ClientException(SuggestionCodeConstants.MISSING_OBJECT_TYPE.name(),
						"Invalid Request | Missing ObjectType parameter");
			}
			String command = (String) map.get(SuggestionCodeConstants.command.name());
			if (StringUtils.isBlank(command) || !map.containsKey(SuggestionCodeConstants.command.name())) {
				throw new ClientException(SuggestionCodeConstants.MISSING_COMMAND.name(),
						"Invalid Request | Missing Command parameter");
			}
			if (null == map.get(SuggestionCodeConstants.params.name()) || !map.containsKey(SuggestionCodeConstants.params.name())) {
				throw new ClientException(SuggestionCodeConstants.MISSING_PARAMS.name(),
						"Invalid Request | Missing params parameter");
			}
			String suggestedBy = (String) map.get(SuggestionCodeConstants.suggestedBy.name());
			if (StringUtils.isBlank(suggestedBy) || !map.containsKey(SuggestionCodeConstants.suggestedBy.name())) {
				throw new ClientException(SuggestionCodeConstants.MISSING_SUGGESTED_BY.name(),
						"Invalid Request | Missing SuggestedBy parameter");
			} else {
				LOGGER.log("Returning requestMap if validation is successful");
				return map;
			}
		}
		LOGGER.log("Returning null if request is emptty");
		return null;
	}
}
