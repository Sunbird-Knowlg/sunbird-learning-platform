package org.sunbird.framework.controller;

import java.util.Map;

import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.framework.mgr.ICategoryInstanceManager;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This is the entry point for all CRUD operations related to categoryInstance for channel API.
 * 
 * @author Rashmi
 *
 */
@Controller
@RequestMapping("/channel/v3/category")
public class ChannelCategoryV3Controller extends BaseController {

	@Autowired
	private ICategoryInstanceManager categoryInstanceManager;
	
	/**
	 * 
	 * @param requestMap
	 * @param channelId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap, 
			@RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.learning.categoryinstance.create";
		Request request = getRequest(requestMap);
		try {
			if(categoryInstanceManager.validateScopeId(channelId)) {
				Map<String, Object> map = (Map<String, Object>) request.get("category");
				Response response = categoryInstanceManager.createCategoryInstance(channelId, map);
				return getResponseEntity(response, apiId, null);
			} else {
				throw new ClientException("ERR_INVALID_CHANNEL_ID", "Invalid channelId: " + channelId + " for Category instance", apiId, null);
			}
		} catch (Exception e) {
			TelemetryManager.error("Create category instance: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param categoryInstanceId
	 * @param channelId
	 * @return
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String categoryInstanceId,
			@RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.learning.categoryinstance.read";
		try {
			if(categoryInstanceManager.validateScopeId(channelId)) {
				Response response = categoryInstanceManager.readCategoryInstance(channelId, categoryInstanceId);
				return getResponseEntity(response, apiId, null);
			} else {
				throw new ClientException("ERR_INVALID_CHANNEL_ID", "Invalid channelId: " + channelId + " for Category instance", apiId, null);
			}
		} catch (Exception e) {
			TelemetryManager.error("Read category instance: "+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * 
	 * @param categoryInstanceId
	 * @param requestMap
	 * @param channelId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String categoryInstanceId,
			@RequestBody Map<String, Object> requestMap, @RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.learning.categoryinstance.update";
		Request request = getRequest(requestMap);
		try {
			if(categoryInstanceManager.validateScopeId(channelId)) {
				Map<String, Object> map = (Map<String, Object>) request.get("category");
				Response response = categoryInstanceManager.updateCategoryInstance(channelId, categoryInstanceId, map);
				return getResponseEntity(response, apiId, null);
			} else {
				throw new ClientException("ERR_INVALID_CHANNEL_ID", "Invalid channelId: " + channelId + " for Categoryinstance", apiId, null);
			}
		} catch (Exception e) {
			TelemetryManager.error("Update category instance: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	
	/**
	 * 
	 * @param map
	 * @param channelId
	 * @return
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	@SuppressWarnings({"unchecked", "rawtypes"})
	public ResponseEntity<Response> search(@RequestBody Map<String, Object> map, @RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.learning.categoryinstance.search";
		Request request = getRequest(map);
		try {
			if(categoryInstanceManager.validateScopeId(channelId)) {	
				Response response = categoryInstanceManager.searchCategoryInstance(channelId, (Map)request.get("search"));
				return getResponseEntity(response, apiId, null);
			} else {
				throw new ClientException("ERR_INVALID_CHANNEL_ID", "Invalid channelId: " + channelId + " for CategoryInstance", apiId, null);
			}
		} catch (Exception e) {
			TelemetryManager.error("search category instance | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * 
	 * @param categoryInstanceId
	 * @param channelId
	 * @return
	 */
	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String categoryInstanceId,  
			@RequestHeader(value = "X-Channel-id") String channelId) {
		String apiId = "ekstep.learning.categoryinstance.retire";
		try {
			if(categoryInstanceManager.validateScopeId(channelId)) {
				Response response = categoryInstanceManager.retireCategoryInstance(channelId, categoryInstanceId);
				return getResponseEntity(response, apiId, null);
			} else {
					throw new ClientException("ERR_INVALID_CHANNEL_ID", "Invalid channelId: " + channelId + " for Categoryinstance", apiId, null);
			}
		} catch (Exception e) {
			TelemetryManager.error("retire category instance | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}