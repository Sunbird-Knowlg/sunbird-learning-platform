/**
 * 
 */
package com.ilimi.framework.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.framework.mgr.ITermManager;

/**
 * @author pradyumna
 *
 */
@Controller
@RequestMapping("/v3/framework/term")
public class TermFrameworkV3Controller extends BaseController {

	@Autowired
	private ITermManager termManager;

	private String graphId = "domain";

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.framework.term.create";
		PlatformLogger.log("Executing Term Create API (Java Version) (API Version V3).", requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("term");
			if (termManager.validateRequest(frameworkId, categoryId)) {
				Response response = termManager.createTerm(categoryId, map);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(
						new ClientException("ERR_INVALID_CATEGORY_ID", "Invalid CategoryId for TermCreation"), apiId,
						null);
			}
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}

	}

	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String termId,
			@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.framework.term.read";
		PlatformLogger.log("Executing term Get API term Id: " + termId + ".", null);
		Response response;
		PlatformLogger.log("category GetById | term Id : " + termId);
		try {
			if (termManager.validateRequest(frameworkId, categoryId) && termId.contains(categoryId)) {
				PlatformLogger.log("Calling the Manager for fetching category 'getById' | [term Id " + termId + "]");
				response = termManager.readTerm(graphId, termId);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(
						new ClientException("ERR_INVALID_CATEGORY_ID", "Invalid CategoryId for Term"), apiId, null);
			}
		} catch (Exception e) {
			PlatformLogger.log("Read term", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String termId,
			@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.framework.term.search";
		PlatformLogger.log("Executing term update API For term Id: " + termId + ".", requestMap, "INFO");
		Request request = getRequest(requestMap);
		try {
			if (termManager.validateRequest(frameworkId, categoryId)) {
				Map<String, Object> map = (Map<String, Object>) request.get("term");
				Response response = termManager.updateTerm(categoryId, termId, map);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(
						new ClientException("ERR_INVALID_CATEGORY_ID", "Invalid CategoryId for TermUpdate"), apiId,
						null);
			}

		} catch (Exception e) {
			PlatformLogger.log("Update term", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.framework.term.search";
		PlatformLogger.log("Executing term search API For category Id: " + categoryId + ".", "INFO");
		Request request = getRequest(requestMap);
		try {
			if (termManager.validateRequest(frameworkId, categoryId)) {
				Map<String, Object> map = (Map<String, Object>) request.get("search");
				Response response = termManager.searchTerms(categoryId, map);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(
						new ClientException("ERR_INVALID_CATEGORY_ID", "Invalid CategoryId for TermSearch"), apiId,
						null);
			}
		} catch (Exception e) {
			PlatformLogger.log("Search terms", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String termId,
			@RequestParam(value = "framework", required = true) String frameworkId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.framework.term.retire";
		PlatformLogger.log("Executing term retire API For term Id: " + termId + ".", "INFO");
		try {
			if (termManager.validateRequest(frameworkId, categoryId)) {
				Response response = termManager.retireTerm(categoryId, termId);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(
						new ClientException("ERR_INVALID_CATEGORY_ID", "Invalid CategoryId for TermRetire"), apiId,
						null);
			}

		} catch (Exception e) {
			PlatformLogger.log("Update term", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

}
