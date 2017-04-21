package com.ilimi.taxonomy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
			String suggestionId = suggestionManager.createSuggestion(request);
			Response response = new Response();
			response.setId(suggestionId);
			System.out.println(response.get(suggestionId));
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
			if (null == map.get("identifier")) {
				throw new ClientException(SuggestionErrorCodeConstants.Missing_contentId.name(),
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
