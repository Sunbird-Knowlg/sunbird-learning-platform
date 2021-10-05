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
 * Actions to handle master category terms.
 */
@Controller
@RequestMapping("/framework/v3/category/term")
public class TermCategoryV3Controller extends BaseController {

	@Autowired
	private ITermManager termManager;

	/**
	 * 
	 * @param categoryId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.category.term.create";
		Request request = getRequest(requestMap);
		try {
			Response response = termManager.createTerm(null, categoryId, request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while creating term (Create term API): "+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String termId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.learning.category.term.read";
		try {
			Response response = termManager.readTerm(null, termId, categoryId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Read term" + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param categoryId
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String termId,
			@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.category.term.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("term");
			Response response = termManager.updateTerm(null, categoryId, termId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Update term"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param categoryID
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestParam(value = "category", required = true) String categoryID,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.category.term.search";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("search");
			Response response = termManager.searchTerms(null, categoryID, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Search terms"+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param termId
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String termId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.learning.category.term.retire";
		try {
			Response response = termManager.retireTerm(null, categoryId, termId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("retire term: "+ e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
