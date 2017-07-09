package org.ekstep.language.controller;

import java.util.Map;

import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.PlatformLogger;

/**
 * The Class ToolsController is the entry point for computing and fetching the
 * Complexity components of words or texts.
 * 
 * @author Rayulu
 */
@Controller
@RequestMapping("v1/language/tools")
public class ToolsController extends BaseLanguageController {

	/** The logger. */
	

	/**
	 * Computes and returns the complexity measures of a word or a text.
	 *
	 * @param map
	 *            the map
	 * @return the complexity
	 */
	@RequestMapping(value = "/complexityMeasures", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getComplexity(@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.language.complexityMeasures.list";
		Request request = getRequest(map);
		String language = (String) request.get(LanguageParams.language_id.name());
		// TODO: return error response if language value is blank
		request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
		request.setOperation(LanguageOperations.computeComplexity.name());
		request.getContext().put(LanguageParams.language_id.name(), language);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = getResponse(request);
			PlatformLogger.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Computes and returns the complexity measures of a text.
	 *
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/complexityMeasures/text", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> computeTextComplexity(@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.language.text.complexity.info";
		Request request = getRequest(map);
		String language = (String) request.get(LanguageParams.language_id.name());
		request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
		request.setOperation(LanguageOperations.computeTextComplexity.name());
		request.getContext().put(LanguageParams.language_id.name(), language);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = getResponse(request);
			PlatformLogger.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Analyzes texts from a CSV file, computes and returns complexity measures
	 * of those texts.
	 *
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/text/analysis", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> analyseTextsCSV(@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.language.text.analysis.csv";
		Request request = getRequest(map);
		String language = (String) request.get(LanguageParams.language_id.name());
		request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
		request.setOperation(LanguageOperations.analyseTextsCSV.name());
		request.getContext().put(LanguageParams.language_id.name(), language);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = getBulkOperationResponse(request);
			PlatformLogger.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Analyzes texts, computes and returns complexity measures of those texts.
	 *
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/textAnalysis", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> analyseTexts(@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.language.text.analysis";
		Request request = getRequest(map);
		String language = (String) request.get(LanguageParams.language_id.name());
		request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
		request.setOperation(LanguageOperations.analyseTexts.name());
		request.getContext().put(LanguageParams.language_id.name(), language);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = getBulkOperationResponse(request);
			PlatformLogger.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Computes the word complexity measure of a given word.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/wordComplexity/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> computeWordComplexityV2(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.language.word.complexity.info";
		Request request = getRequest(map);
		request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
		request.setOperation(LanguageOperations.getWordComplexities.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = getResponse(request);
			PlatformLogger.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Synchronizes the definition from Graph into the Definition Cache.
	 *
	 * @param languageId
	 *            the language id
	 * @param definitionName
	 *            the definition name
	 * @return the response entity
	 */
	@RequestMapping(value = "/syncDefinition/{definitionName}/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> syncDefinition(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "definitionName") String definitionName) {
		String apiId = "ekstep.language.sync.definition";
		Request request = new Request();
		request.put(LanguageParams.definitionName.name(), definitionName);
		request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
		request.setOperation(LanguageOperations.syncDefinition.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = getResponse(request);
			PlatformLogger.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}
	
	@RequestMapping(value = "/enrich/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> enrichWord(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.language.word.complexity.info";
		Request request = getRequest(map);
		request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
		request.setOperation(LanguageOperations.enrichWord.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		PlatformLogger.log("List | Request: " + request);
		try {
			Response response = getResponse(request);
			PlatformLogger.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}
}
