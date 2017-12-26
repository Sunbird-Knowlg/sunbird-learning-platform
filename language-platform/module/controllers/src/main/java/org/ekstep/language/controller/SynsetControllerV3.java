package org.ekstep.language.controller;

import org.ekstep.common.dto.Response;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.mgr.IDictionaryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("v3/synsets")
public class SynsetControllerV3 extends BaseLanguageController {

	@Autowired
	private IDictionaryManager dictionaryManager;
	
	

	@RequestMapping(value = "/read/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> find(@RequestParam(value = "language_id", required = true) String languageId,
			@PathVariable(value = "objectId") String objectId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".info";
		try {
			Response response = dictionaryManager.getSynsetV3(languageId, objectId);
			PlatformLogger.log("Find | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Find | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	protected String getObjectType() {
		return LanguageObjectTypes.Synset.name();
	}
}
