/**
 * 
 */
package com.ilimi.framework.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/v3/channel/term")
public class TermChannelV3Controller extends BaseController {

	@Autowired
	private ITermManager termManager;

	private String graphId = "domain";

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.framework.term.create";
		PlatformLogger.log("Executing Term Create API (Java Version) (API Version V3).", requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("term");
			if (StringUtils.isNotBlank(categoryId)) {
				Response response = termManager.createTerm(categoryId, map);
				return getResponseEntity(response, apiId, null);
			}else {
				return getExceptionResponseEntity(
						new ClientException("ERR_TERM_CATEGORY_ID_BLANK", "Required CategoryId is missing"), apiId,
						null);
			}
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}

	}

	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> read(@PathVariable(value = "id") String termId,
			@RequestParam(value = "category", required = true) String categoryId) {
		String apiId = "ekstep.framework.term.read";
		PlatformLogger.log("Executing term Get API term Id: " + termId + ".", null);
		Response response;
		PlatformLogger.log("category GetById | term Id : " + termId);
		try {
			if (StringUtils.isNotBlank(categoryId)) {
				PlatformLogger
						.log("Calling the Manager for fetching category 'getById' | [term Id " + termId + "]");
				response = termManager.readTerm(graphId, termId);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(new ClientException("ERR_", "Required CategoryId is missing"), apiId,
						null);
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
			@RequestParam(value = "category", required = true) String categoryId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.term.search";
		PlatformLogger.log(
				"Executing term update API For term Id: " + termId + ".",
				requestMap, "INFO");
		Request request = getRequest(requestMap);
		try {
			if (StringUtils.isNotBlank(categoryId)) {
				Map<String, Object> map = (Map<String, Object>) request.get("term");
				Response response = termManager.updateTerm(categoryId, termId, map);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(new ClientException("ERR_", "Required CategoryId is missing"), apiId,
						null);
			}

		} catch (Exception e) {
			PlatformLogger.log("Update term", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> list(@RequestParam(value = "category", required = true) String categoryID) {
		String apiId = "ekstep.learning.term.update";
		PlatformLogger.log("Executing term list API For category Id: " + categoryID + ".", "INFO");
		try {
			if (StringUtils.isNotBlank(categoryID)) {
				Response response = termManager.searchTerms(categoryID);
				return getResponseEntity(response, apiId, null);
			} else {
				return getExceptionResponseEntity(new ClientException("ERR_", "Required CategoryId is missing"), apiId,
						null);
			}

		} catch (Exception e) {
			PlatformLogger.log("List terms", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

}
