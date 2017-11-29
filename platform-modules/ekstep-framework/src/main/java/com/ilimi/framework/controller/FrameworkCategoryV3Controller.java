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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.framework.mgr.ICategoryInstanceManager;

/**
 * This is the entry point for all CRUD operations related to categoryInstance for Framework API.
 * 
 * @author Rashmi
 *
 */
@Controller
@RequestMapping("/v3/framework/category")
public class FrameworkCategoryV3Controller extends BaseController {

@Autowired
private ICategoryInstanceManager categoryInstanceManager;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap, 
			@RequestParam(value = "frameworkId", required = true) String frameworkId) {
		String apiId = "ekstep.learning.categoryInstance.create";
		PlatformLogger.log("Executing category Create API.", requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("categoryInstance");
			Response response = categoryInstanceManager.createCategoryInstance(frameworkId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Create category instance", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String categoryInstanceId,
			@RequestParam(value = "frameworkId", required = true) String frameworkId) {
		String apiId = "ekstep.learning.categoryInstance.read";
		PlatformLogger.log(
				"Executing category instance Get API category instance Id: " + categoryInstanceId + ".", null);
		Response response;
		PlatformLogger.log("category instance GetById | category instance Id : " + categoryInstanceId);
		try {
			PlatformLogger.log("Calling the Manager for fetching category instance 'getById' | [category instance Id " + categoryInstanceId + "]");
			response = categoryInstanceManager.readCategoryInstance(frameworkId, categoryInstanceId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Read category instance", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String categoryInstanceId,
			@RequestParam(value = "frameworkId", required = true) String frameworkId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.categoryInstance.update";
		PlatformLogger.log("Executing category Update API For category instance Id: " + categoryInstanceId + ".",requestMap, "INFO");
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("categoryInstance");
			Response response = categoryInstanceManager.updateCategoryInstance(frameworkId, categoryInstanceId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Update category", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestBody Map<String, Object> map,
			@RequestParam(value = "frameworkId", required = true) String frameworkId) {
		String apiId = "ekstep.learning.categoryInstance.search";
		PlatformLogger.log("search | category: " + " | Request: " + map);
		try {
			Response response = categoryInstanceManager.searchCategoryInstance(frameworkId, map);
			PlatformLogger.log("search category instance | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("search category instance | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String categoryInstanceId,
			@RequestParam(value = "frameworkId", required = true) String frameworkId) {
		String apiId = "ekstep.learning.categoryInstance.retire";
		PlatformLogger.log("Get | categorys: " + " | Request: " + categoryInstanceId);
		try {
			Response response = categoryInstanceManager.retireCategoryInstance(frameworkId, categoryInstanceId);
			PlatformLogger.log("retire category | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("retire category instance | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}