package org.sunbird.framework.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.framework.mgr.IFrameworkManager;
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
 * Controller Class for Framework API in LP
 * 
 * @author gauraw
 *
 */
@Controller
@RequestMapping("/framework/v3")
public class FrameworkV3Controller extends BaseController {

	@Autowired
	private IFrameworkManager frameworkManager;

	/**
	 * 
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> createFramework(@RequestBody Map<String, Object> requestMap,
			@RequestHeader(value = "X-Channel-Id") String channelId) {

		String apiId = "ekstep.learning.framework.create";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("framework");
			Response response = frameworkManager.createFramework(map, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager
					.error("Exception Occured while creating framework (Create Framework API): " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param frameworkId
	 * @return
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> readFramework(@PathVariable(value = "id") String frameworkId,
			@RequestParam(value = "categories", required = false) String[] categories) {
		String apiId = "ekstep.learning.framework.read";
		try {
			List<String> returnCategories = (categories == null) ? Arrays.asList() : Arrays.asList(categories);
			Response response = frameworkManager.readFramework(frameworkId, returnCategories);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error(
					"Exception Occured while reading framework details (Read Framework API): " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param frameworkId
	 * @param requestMap
	 * @param channelId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> updateFramework(@PathVariable(value = "id") String frameworkId,
			@RequestBody Map<String, Object> requestMap, @RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.learning.framework.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("framework");
			Response response = frameworkManager.updateFramework(frameworkId, channelId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager
					.error("Exception Occured while updating framework (Update Framework API): " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> listFramework(@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.framework.list";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("search");
			Response response = frameworkManager.listFramework(map);
			return getResponseEntity(response, apiId, null);

		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while Performing List Operation : " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param frameworkId
	 * @param userId
	 * @param channelId
	 * @return
	 */
	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String frameworkId,
			@RequestHeader(value = "user-id") String userId, @RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.learning.framework.retire";
		try {
			Response response = frameworkManager.retireFramework(frameworkId, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while Performing Retire Operation : " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * @param frameworkId
	 * @param channelId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/copy/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> copyFramework(@PathVariable(value = "id") String frameworkId,
			@RequestBody Map<String, Object> requestMap, @RequestHeader(value = "X-Channel-Id") String channelId) {

		String apiId = "ekstep.learning.framework.copy";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("framework");
			Response response = frameworkManager.copyFramework(frameworkId, channelId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while copying framework (Copy Framework API): " + e.getMessage(),
					e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param frameworkId
	 * @param channelId
	 * @return
	 */
	@RequestMapping(value = "/publish/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> publish(@PathVariable(value = "id") String frameworkId,
			@RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.learning.framework.publish";
		try {
			Response response = frameworkManager.publishFramework(frameworkId, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager
					.error("Exception Occured while publishing framework (Publish Framework API): " + e.getMessage(),
							e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}