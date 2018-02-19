package org.ekstep.learning.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.learning.mgr.IFormManager;
import org.ekstep.telemetry.logger.TelemetryManager;
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

/*
 * @author Mahesh
 */

@Controller
@RequestMapping("/v3/form")
public class FormController extends BaseController {

	@Autowired
	private IFormManager formManager;

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestHeader(value = "X-Channel-ID", required = true) String channel,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.form.create";
		Request request = getRequest(requestMap);
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) request.get(formManager.getObjectType());
			Response response = formManager.create(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{type:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "type") String type,
			@RequestParam(value = "operation", required = true) String operation,
			@RequestHeader(value = "X-Channel-ID", required = true) String channel,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.form.update";
		Request request = getRequest(requestMap);
		try {
			String identifier = formManager.getIdentifier(type, operation, channel);
			Map<String, Object> map = (Map<String, Object>) request.get(formManager.getObjectType());
			
			Response response = formManager.update(identifier, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Form update error: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/read/{type:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "type") String type,
			@RequestParam(value = "operation", required = true) String operation,
			@RequestHeader(value = "X-Channel-ID", required = true) String channel) {
		String apiId = "ekstep.learning.form.info";
		Response response;
		try {
			String identifier = formManager.getIdentifier(type, operation, channel);
			response = formManager.read(identifier, type, operation);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/retire/{type:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "type") String type,
			@RequestParam(value = "operation", required = true) String operation,
			@RequestHeader(value = "X-Channel-ID", required = true) String channel) {
		String apiId = "ekstep.learning.form.retire";
		Response response;
		try {
			String identifier = formManager.getIdentifier(type, operation, channel);
			response = formManager.read(identifier, type, operation);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("retire category | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
