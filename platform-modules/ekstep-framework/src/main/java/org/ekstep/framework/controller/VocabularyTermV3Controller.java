/**
 * 
 */
package org.ekstep.framework.controller;

import java.util.Map;

import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.framework.mgr.IVocabularyTermMgr;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author pradyumna
 *
 */
@Controller
@RequestMapping("/v3/vocabulary/term")
public class VocabularyTermV3Controller extends BaseController {

	@Autowired
	IVocabularyTermMgr vocabularyTermMgr;

	/**
	 * 
	 * @param categoryId
	 * @param requestMap
	 * @return
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.vocabulary.term.create";
		Request request = getRequest(requestMap);
		try {
			Response response = vocabularyTermMgr.create(request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while creating term (Create term API): " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param categoryId
	 * @param requestMap
	 * @return
	 */
	@RequestMapping(value = "/suggest", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> suggest(@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.vocabulary.term.suggest";
		Request request = getRequest(requestMap);
		try {
			Response response = vocabularyTermMgr.suggest(request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception Occured while creating term (Create term API): " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

}
