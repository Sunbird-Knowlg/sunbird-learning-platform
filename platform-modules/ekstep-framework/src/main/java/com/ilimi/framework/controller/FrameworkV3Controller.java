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
import com.ilimi.framework.mgr.IFrameworkManager;

/**
 * Controller Class for Framework API in LP
 * 
 * @author gauraw
 *
 */
@Controller
@RequestMapping("/v3/framework")
public class FrameworkV3Controller extends BaseController{

	private final String graphId = "domain";
	
	@Autowired
	private IFrameworkManager frameworkManager;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> createFramework(@RequestBody Map<String, Object> requestMap) {

		String apiId = "ekstep.learning.framework.create";
		PlatformLogger.log("Executing framework Create API for Request : ", requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("framework");
			Response response = frameworkManager.createFramework(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while creating framework (Create Framework API): ", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> readFramework(@PathVariable(value = "id") String frameworkId) {

		String apiId = "ekstep.learning.framework.read";
		PlatformLogger.log(
				"Executing framework Read API for Framework Id : " + frameworkId);

		try {
			Response response = frameworkManager.readFramework(graphId,frameworkId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while reading framework details (Read Framework API): ", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> updateFramework(@PathVariable(value = "id") String frameworkId,
			@RequestBody Map<String, Object> requestMap) {

		String apiId = "ekstep.learning.framework.update";
		PlatformLogger.log(
				"Executing framework Update API for Framework Id : " + frameworkId,
				requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("framework");
			Response response = frameworkManager.updateFramework(frameworkId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while updating framework (Update Framework API): ", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> listFramework(@RequestBody Map<String, Object> requestMap) {

		String apiId = "ekstep.learning.framework.list";
		PlatformLogger.log("Executing framework List API : ", requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("framework");
			Response response = frameworkManager.listFramework(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
}