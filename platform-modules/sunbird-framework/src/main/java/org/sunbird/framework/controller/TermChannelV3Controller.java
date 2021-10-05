package org.sunbird.framework.controller;

import java.util.Map;

import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.framework.mgr.ITermManager;
import org.sunbird.telemetry.logger.TelemetryManager;
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

/**
 * @author pradyumna
 *
 */
@Controller
@RequestMapping("channel/v3/term")
public class TermChannelV3Controller extends BaseController {

	@Autowired
	private ITermManager termManager;

	/**
	 * 
	 * @param categoryId
	 * @param channelId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestParam(value = "category", required = true) String categoryId,
			@RequestHeader(value = "X-Channel-Id") String channelId, @RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.channel.term.create";
		Request request = getRequest(requestMap);
		try {
			Response response = termManager.createTerm(channelId, categoryId, request);
			return getResponseEntity(response, apiId, null);

		} catch (Exception e) {
			TelemetryManager.error("create term" + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param channelId
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String termId,
			@RequestHeader(value = "X-Channel-Id") String channelId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.learning.channel.term.read";
		try {
			Response response = termManager.readTerm(channelId, termId, categoryId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Read term"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param channelId
	 * @param categoryId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String termId,
			@RequestHeader(value = "X-Channel-Id") String channelId,
			@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.channel.term.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("term");
			Response response = termManager.updateTerm(channelId, categoryId, termId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Update term"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param categoryId
	 * @param channelId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> list(@RequestParam(value = "category", required = true) String categoryId,
			@RequestHeader(value = "X-Channel-Id") String channelId, @RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.channel.term.search";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("search");
			Response response = termManager.searchTerms(channelId, categoryId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Search terms"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param channelId
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String termId,
			@RequestHeader(value = "X-Channel-Id") String channelId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.learning.channel.term.retire";
		try {
			Response response = termManager.retireTerm(channelId, categoryId, termId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("retire term"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}