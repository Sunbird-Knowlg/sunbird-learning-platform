package com.ilimi.framework.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.framework.mgr.IChannelManager;

@Controller
@RequestMapping("/v3/channel")
public class ChannelV3Controller extends BaseController {

	@Autowired
	private IChannelManager channelManager;
	
	private String graphId = "domain";
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.channel.create";
		PlatformLogger.log("Executing channel Create API.", requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("channel");
			Response response = channelManager.createChannel(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Create Channel", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String channelId) {
		String apiId = "ekstep.learning.channel.read";
		PlatformLogger.log(
				"Executing Channel Get API Channel Id: " + channelId + ".", null);
		Response response;
		PlatformLogger.log("Channel GetById | Channel Id : " + channelId);
		try {
			PlatformLogger.log("Calling the Manager for fetching Channel 'getById' | [channel Id " + channelId + "]");
			response = channelManager.readChannel(graphId, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Read Channel", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String channelId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.channel.update";
		PlatformLogger.log(
				"Executing channel Update API For channel Id: " + channelId + ".",
				requestMap, "INFO");
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("channel");
			Response response = channelManager.updateChannel(channelId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Update Channel", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> list(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.channel.list";
		PlatformLogger.log("Get | channels: " + " | Request: " + map);
		try {
			Response response = channelManager.listChannel(map);
			PlatformLogger.log("List Channel | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("List Channel | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
