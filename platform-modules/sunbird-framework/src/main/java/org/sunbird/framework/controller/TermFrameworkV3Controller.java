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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author pradyumna
 *
 */
@Controller
@RequestMapping("/framework/v3/term")
public class TermFrameworkV3Controller extends BaseController {

	@Autowired
	private ITermManager termManager;

	/**
	 * 
	 * @param frameworkId
	 * @param category
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String category,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.framework.term.create";
		Request request = getRequest(requestMap);
		try {
			Response response = termManager.createTerm(frameworkId, category, request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("create term"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param frameworkId
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String termId,
			@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.learning.framework.term.read";
		try {
			Response response = termManager.readTerm(frameworkId, termId, categoryId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Read term"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param frameworkId
	 * @param category
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String termId,
			@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String category,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.framework.term.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("term");
			Response response = termManager.updateTerm(frameworkId, category, termId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Update term" +e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param frameworkId
	 * @param categoryId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.framework.term.search";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("search");
			Response response = termManager.searchTerms(frameworkId, categoryId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Search terms"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param frameworkId
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String termId,
			@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.learning.framework.term.retire";
		try {
			Response response = termManager.retireTerm(frameworkId, categoryId, termId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("retire term"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}