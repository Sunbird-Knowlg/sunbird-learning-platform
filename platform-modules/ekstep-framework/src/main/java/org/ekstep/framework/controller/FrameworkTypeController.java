package org.ekstep.framework.controller;

import java.util.Map;

import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.framework.mgr.IFrameworkTypeManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author mahesh
 *
 */

@Controller
@RequestMapping("/v3/framework/type")
public class FrameworkTypeController extends BaseController  {
	
	@Autowired
	private IFrameworkTypeManager manager;

	/**
	 * 
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap) {

		String apiId = "ekstep.learning.framework.type.create";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("frameworktype");
			Response response = manager.create(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while creating framework (Create Framework API): "+ e.getMessage(), e);
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
	public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.framework.type.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("frameworktype");
			Response response = manager.update(id, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while updating framework (Update Framework API): "+ e.getMessage(), e);
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
	public ResponseEntity<Response> list(@RequestBody Map<String, Object> requestMap, @RequestParam(value = "refresh", required = false) boolean updateCache) {
		String apiId = "ekstep.learning.framework.type.list";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("search");
			Response response = manager.list(map, updateCache);
			return getResponseEntity(response, apiId, null);
			
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while Performing List Operation : " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

}
