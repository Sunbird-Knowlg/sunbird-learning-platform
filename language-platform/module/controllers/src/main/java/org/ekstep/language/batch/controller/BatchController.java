package org.ekstep.language.batch.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.batch.mgr.IBatchManager;
import org.ekstep.language.batch.mgr.IWordnetCSVManager;
import org.ekstep.language.controller.BaseLanguageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Response;

/**
 * The Class BatchController. provide batch operations like correct wordnet
 * data, update wordChians
 *
 * @author rayulu, karthik
 */
@Controller
@RequestMapping("v1/language/batch")
public class BatchController extends BaseLanguageController {

	/** The batch manager. */
	@Autowired
	private IBatchManager batchManager;

	/** The wordnet CSV manager. */
	@Autowired
	private IWordnetCSVManager wordnetCSVManager;

	private static Logger LOGGER = LogManager.getLogger(BatchController.class.getName());

	/**
	 * Correct wordnet data.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/correctWordnetData", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> correctWordnetData(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.correctWordnetData";
		try {
			Response response = batchManager.correctWordnetData(languageId);
			LOGGER.info("correctWordnetData | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("correctWordnetData | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Update pictures.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/updatePictures", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> updatePictures(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.updatePictures";
		try {
			Response response = batchManager.updatePictures(languageId);
			LOGGER.info("updatePictures | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("updatePictures | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Cleanup word net data.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/cleanupWordNetData", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> cleanupWordNetData(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.cleanupWordNetData";
		try {
			Response response = batchManager.cleanupWordNetData(languageId);
			LOGGER.info("cleanupWordNetData | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("cleanupWordNetData | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Update word chain.
	 *
	 * @param languageId
	 *            the language id
	 * @param start
	 *            the start
	 * @param total
	 *            the total
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/updateWordChain", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> updateWordChain(@PathVariable(value = "languageId") String languageId,
			@RequestParam(name = "start", required = false) Integer start,
			@RequestParam(name = "total", required = false) Integer total) {
		String apiId = "language.updateWordChain";
		try {
			Response response = batchManager.updateWordChain(languageId, start, total);
			LOGGER.info("updateWordChain | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("updateWordChain | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Sets the primary meaning.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/setPrimaryMeaning", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> setPrimaryMeaning(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.setPrimaryMeaning";
		try {
			Response response = batchManager.setPrimaryMeaning(languageId);
			LOGGER.info("setPrimaryMeaning | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("setPrimaryMeaning | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Update pos list.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/updatePosList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> updatePosList(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.updatePosList";
		try {
			Response response = batchManager.updatePosList(languageId);
			LOGGER.info("updatePosList | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("updatePosList | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Update word complexity.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/updateWordComplexity", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> updateWordComplexity(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.updateWordComplexity";
		try {
			Response response = batchManager.updateWordComplexity(languageId);
			LOGGER.info("updateWordComplexity | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Update word features.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/updateWordFeatures", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> updateWordFeatures(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.updateWordFeatures";
		try {
			Response response = batchManager.updateWordFeatures(languageId);
			LOGGER.info("updateWordFeatures | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("updateWordFeatures | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Update frequency counts.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/updateFrequencyCounts", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> updateFrequencyCounts(@PathVariable(value = "languageId") String languageId) {
		String apiId = "language.updateFrequencyCounts";
		try {
			Response response = batchManager.updateFrequencyCounts(languageId);
			LOGGER.info("updateFrequencyCounts | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("updateFrequencyCounts | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Creates the wordnet citations.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/createWordnetCitations", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> createWordnetCitations(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map) {
		String apiId = "language.createWordnetCitations";
		try {
			String wordsCSV = (String) map.get("words_csv");
			Response response = wordnetCSVManager.createWordnetCitations(languageId, wordsCSV);
			LOGGER.info("createWordnetCitations | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("createWordnetCitations | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Replace wordnet ids.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/replaceWordnetIds", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> replaceWordnetIds(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map) {
		String apiId = "language.replaceWordnetIds";
		try {
			String wordsCSV = (String) map.get("words_csv");
			String synsetCSV = (String) map.get("synsets_csv");
			String outputDir = (String) map.get("output_dir");
			Response response = wordnetCSVManager.replaceWordnetIds(languageId, wordsCSV, synsetCSV, outputDir);
			LOGGER.info("replaceWordnetIds | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("replaceWordnetIds | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Adds the wordnet indexes.
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/addWordnetIndexes", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> addWordnetIndexes(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map) {
		String apiId = "language.addWordnetIndexes";
		try {
			String wordsCSV = (String) map.get("words_csv");
			Response response = wordnetCSVManager.addWordnetIndexes(languageId, wordsCSV);
			LOGGER.info("addWordnetIndexes | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("addWordnetIndexes | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
