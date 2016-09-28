package org.ekstep.language.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.mgr.IDictionaryManager;
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

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

/**
 * The Class SearchControllerV2, is entry point for search operation
 *
 * @author amarnath
 */
@Controller
@RequestMapping("v2/language/dictionary/search")
public class SearchControllerV2 extends BaseController {

	/** The dictionary manager. */
	@Autowired
	private IDictionaryManager dictionaryManager;

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ParserController.class.getName());

	/**
	 * List all words based on the filters and populates primary meanings and
	 * relations
	 * 
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId,
			@RequestParam(value = "version", required = false, defaultValue = API_VERSION_2) String version) {
		String apiId = "word.search";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.list(languageId, LanguageObjectTypes.Word.name(), request, version);
			LOGGER.info("Search | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.error("Search | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

}
