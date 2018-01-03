package org.ekstep.framework.controller;

import java.util.Map;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.ekstep.common.controller.BaseController;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.telemetry.logger.TelemetryManager;

/**
 * This is the entry point for all CRUD operations related to channel API.
 * 
 * @author rashmi
 *
 */
@Controller
@RequestMapping("/v3/channel")
public class ChannelV3Controller extends BaseController {

	@Autowired
	private IChannelManager channelManager;
	
	/**
	 * 
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.channel.create";
		try {
			Request request = getRequest(requestMap);
			Map<String, Object> map = (Map<String, Object>) request.get("channel");
			Response response = channelManager.createChannel(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.log("Create Channel", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * 
	 * @param channelId
	 * @return
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String channelId) {
		String apiId = "ekstep.learning.channel.read";
		try {
			Response response = channelManager.readChannel(channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.log("Read Channel", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * 
	 * @param channelId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String channelId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.channel.update";
		try {
			Request request = getRequest(requestMap);
			Map<String, Object> map = (Map<String, Object>) request.get("channel");
			Response response = channelManager.updateChannel(channelId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.log("Update Channel", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * 
	 * @param map
	 * @param userId
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> list(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.channel.list";
		try {
			Request request = getRequest(map);
			Response response = channelManager.listChannel((Map)request.get("search"));
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.log("List Channel | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * 
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String channelId) {
		String apiId = "ekstep.learning.channel.retire";
		try {
			Response response = channelManager.retireChannel(channelId);
			TelemetryManager.log("retire channel | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.log("retire channel | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
