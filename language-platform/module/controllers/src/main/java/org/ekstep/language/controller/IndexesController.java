package org.ekstep.language.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;

/**
 * The Class IndexesController is the entry point for Word indexes, Citations
 * and Word info indexes stored in Elasticsearch. Provides APIs to load, get and
 * aggregate Elastic search Word indexes
 * 
 * @author Amarnath
 */
@Controller
@RequestMapping("v1/language/indexes")
public class IndexesController extends BaseLanguageController {

	/** The logger. */
	private static ILogger LOGGER = new PlatformLogger(IndexesController.class.getName());

	/**
	 * Parses a file in Simple Shakti format, retrieves and loads citations into
	 * ES.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the request body
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/loadCitations/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> loadCitations(@PathVariable(value = "languageId") String languageId,
			@RequestParam("zipFile") MultipartFile zipFile,
			@RequestParam(name = "source_type", required = false) String source_type,
			@RequestParam(name = "grade", required = false) String grade,
			@RequestParam(name = "source", required = false) String source,
			@RequestParam(name = "skipCitations", required = false, defaultValue = "true") Boolean skipCitations,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.language.citations.load";
		
		InputStream zipStream = null;
		Request request = new Request();
		try {

			if (null != zipFile) {
				zipStream = zipFile.getInputStream();
			}
			request.put(LanguageParams.input_stream.name(), zipStream);
			request.put(LanguageParams.source_type.name(), source_type);
			request.put(LanguageParams.grade.name(), grade);
			request.put(LanguageParams.source.name(), source);
			request.put(LanguageParams.skipCitations.name(), skipCitations);
			
			request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
			request.setOperation(LanguageOperations.loadCitations.name());
			request.getContext().put(LanguageParams.language_id.name(), languageId);
			LOGGER.log("List | Request: " + request);


			Response response = getBulkOperationResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} finally {
			try {
				if (null != zipStream)
					zipStream.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.", e.getMessage(), e);
			}
		}
	}

	/**
	 * Gets the citations count of a given word grouped by various provided
	 * criteria.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the request criteria
	 * @param userId
	 *            the user id
	 * @return the citations count
	 */
	@RequestMapping(value = "/citationsCount/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getCitationsCount(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.language.citations.count";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.citationsCount.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Gets the citations of a given word from ES.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the request criteria
	 * @param userId
	 *            the user id
	 * @return the citations
	 */
	@RequestMapping(value = "/citations/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getCitations(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.language.citations.count";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.citations.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}
	
	/**
	 * Gets the root words of a given list of words.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the request body
	 * @param userId
	 *            the user id
	 * @return the root words
	 */
	@RequestMapping(value = "/getRootWords/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getRootWords(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.language.rootWords.info";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.getRootWords.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Gets the word's id for the given list of words.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the word id
	 */
	@RequestMapping(value = "/getWordId/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getWordId(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.language.words.info";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.getWordId.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " ,e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Gets the index info document from ES.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the index info
	 */
	@RequestMapping(value = "/getIndexlog/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getIndexlog(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "indexlog.get";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		request.setOperation(LanguageOperations.getIndexInfo.name());
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Adds the word index from the request into ES.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the word index body
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/addWordIndex/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> addWordIndex(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "wordIndex.add";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addWordIndex.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Adds the citation index to ES.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the request body
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/addCitationIndex/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> addCitationIndex(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "citation.add";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.addCitationIndex.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getBulkOperationResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Gets the word metrics such as count grouped by criteria and word info and
	 * citations.
	 *
	 * @param languageId
	 *            the language id
	 * @param userId
	 *            the user id
	 * @return the word metrics
	 */
	@RequestMapping(value = "/getWordMetrics/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getWordMetrics(@PathVariable(value = "languageId") String languageId,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "wordMetrics.get";
		Request request = new Request();
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.getWordMetrics.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Performs a wild card search on ES for a given input.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/wordWildCard/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> wordWildCard(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "word.wildCard";
		Request request = getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.wordWildCard.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Gets the morphological variants of a given word from its root word.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the morphological variants
	 */
	@RequestMapping(value = "/morphologicalVariants/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getMorphologicalVariants(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "word.morphologicalVariants";
		Request request = getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.morphologicalVariants.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Gets the root word info.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the root word info
	 */
	@RequestMapping(value = "/rootWordinfo/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getRootWordlog(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "rootWordInfo.get";
		Request request = getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.rootWordInfo.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Gets the word info.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the word log
	 */
	@SuppressWarnings("unused")
	@RequestMapping(value = "/wordInfo/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public void getWordlog(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "wordinfo.get";
		Request request = getRequestObject(map);
		request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		request.setOperation(LanguageOperations.wordInfo.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			Response response = getResponse(request, LOGGER);
			LOGGER.log("List | Response: " + response);
			String csv = (String) response.getResult().get(LanguageParams.word_info.name());
			if (StringUtils.isNotBlank(csv)) {
				resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
				resp.setContentType("text/csv;charset=utf-8");
				resp.setHeader("Content-Disposition", "attachment; filename=WordInfo.csv");
				resp.getOutputStream().write(csv.getBytes(StandardCharsets.UTF_8));
				resp.getOutputStream().close();
			}
		} catch (Exception e) {
			LOGGER.log("List | Exception: " , e.getMessage(), e);
		}
	}

}
