package org.ekstep.language.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.language.mgr.IDictionaryManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


/**
 * Entry point for all v1 CRUD operations on Word and relations.
 * 
 * @author Amarnath, Azhar, Rayulu
 */
public abstract class DictionaryController extends BaseLanguageController {

	/** The dictionary manager. */
	@Autowired
	private IDictionaryManager dictionaryManager;
	
	@Autowired
	private WordControllerV2 wordController;

	/** The logger. */
	

	/**
	 * Uploads a media and returns the Amazon s3 URL
	 *
	 * @param file
	 *            the file
	 * @return the response entity
	 */
	@RequestMapping(value = "/media/upload", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> upload(@RequestParam(value = "file", required = true) MultipartFile file) {
		return wordController.upload(file);
	}

	/**
	 * Finds and returns the word.
	 *
	 * @param languageId
	 *            the language/graph id
	 * @param objectId
	 *            the word id
	 * @param fields
	 *            the fields from the word that should be returned
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> find(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestHeader(value = "user-id") String userId) {
		return wordController.find(languageId, objectId, fields, userId, API_VERSION);
	}

	/**
	 * Find and return all words.
	 *
	 * @param languageId
	 *            the language/graph id
	 * @param fields
	 *            the fields from the word that should be returned
	 * @param limit
	 *            the result limit
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAll(@PathVariable(value = "languageId") String languageId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestHeader(value = "user-id") String userId) {
		return wordController.findAll(languageId, fields, limit, userId, API_VERSION);
	}

	/**
	 * Find and return words using its lemma given in the CSV.
	 *
	 * @param languageId
	 *            the language id
	 * @param file
	 *            the file
	 * @param userId
	 *            the user id
	 * @param response
	 *            the response
	 * @return the response entity
	 */
	@RequestMapping(value = "/findByCSV/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> findWordsCSV(@PathVariable(value = "languageId") String languageId,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse response) {
		return wordController.findWordsCSV(languageId, file, userId, response);
	}

	/**
	 * Import words and their synsets from a CSV file.
	 *
	 * @param languageId
	 *            the language id
	 * @param file
	 *            the CSV file
	 * @param userId
	 *            the user id
	 * @return the response entity
	 
	@RequestMapping(value = "/importWords/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importWordSynset(@PathVariable(value = "languageId") String languageId,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".importWords";
		try {
			Response response = dictionaryManager.importWordSynset(languageId, file.getInputStream());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	*/

	/**
	 * Delete relations between two nodes.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectId1
	 *            the start node
	 * @param relation
	 *            the relation
	 * @param objectId2
	 *            the end node
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/{objectId1:.+}/{relation}/{objectId2:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> deleteRelation(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId1") String objectId1, @PathVariable(value = "relation") String relation,
			@PathVariable(value = "objectId2") String objectId2, @RequestHeader(value = "user-id") String userId) {
		return wordController.deleteRelation(languageId, objectId1, relation, objectId2, userId);
	}

	/**
	 * Adds the relation between two nodes.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectId1
	 *            the start node
	 * @param relation
	 *            the relation
	 * @param objectId2
	 *            the end node
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/{objectId1:.+}/{relation}/{objectId2:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> addRelation(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId1") String objectId1, @PathVariable(value = "relation") String relation,
			@PathVariable(value = "objectId2") String objectId2, @RequestHeader(value = "user-id") String userId) {
		return wordController.addRelation(languageId, objectId1, relation, objectId2, userId);
	}

	/**
	 * Load words arpabets map on Redis from file.
	 *
	 * @param wordsArpabetsFile
	 *            the words arpabets file
	 * @param userId
	 *            the user id
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "/loadWordsArpabetsMap/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> loadWordsArpabetsMap(
			@RequestParam("wordsArpabetsFile") MultipartFile wordsArpabetsFile,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String objectType = getObjectType();
		String apiId = objectType + ".loadWordsArpabetsMap";
		InputStream wordsArpabetsStream = null;
		try {
			if (null != wordsArpabetsFile)
				wordsArpabetsStream = wordsArpabetsFile.getInputStream();
			Response response = dictionaryManager.loadEnglishWordsArpabetsMap(wordsArpabetsStream);
			TelemetryManager.log("loadWordsArpabetsMap | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (IOException e) {
			e.printStackTrace();
			TelemetryManager.error("loadWordsArpabetsMap | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			if (null != wordsArpabetsStream)
				try {
					wordsArpabetsStream.close();
				} catch (IOException e) {
					TelemetryManager.error("Error! While Closing the Input Stream." + e.getMessage(), e);
				}
		}
	}

	/**
	 * Gets the syllables for a given word.
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @param userId
	 *            the user id
	 * @return the syllables
	 */
	@RequestMapping(value = "/{languageId}/syllables/{word:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getSyllables(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "word") String word, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".Syllable";
		try {
			Response response = dictionaryManager.getSyllables(languageId, word);
			TelemetryManager.log("Get Syllables | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Gets the arpabets for a given word.
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @param userId
	 *            the user id
	 * @return the arpabets
	 */
	@RequestMapping(value = "/{languageId}/arpabets/{word:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getArpabets(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "word") String word, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".Arpabets";
		try {
			Response response = dictionaryManager.getArpabets(languageId, word);
			TelemetryManager.log("Get Arpabets | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Gets the phonetic spelling for a given word.
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @param addEndVirama
	 *            if virama should be added at end of the words that end with a
	 *            consonant
	 * @param userId
	 *            the user id
	 * @return the phonetic spelling
	 */
	@RequestMapping(value = "/{languageId}/phoneticSpelling/{word:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getPhoneticSpelling(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "word") String word,
			@RequestParam(name = "addClosingVirama", defaultValue = "false") boolean addEndVirama,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".PhoneticSpelling";
		try {
			Response response = dictionaryManager.getPhoneticSpellingByLanguage(languageId, word, addEndVirama);
			TelemetryManager.log("Get PhoneticSpelling | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Transliterates an english text into a given language
	 * 
	 * @param languageId
	 *            code of the language into which the text should be
	 *            transliterated
	 * @param addEndVirama
	 *            if virama should be added at end of the words that end with a
	 *            consonant
	 * @param map
	 *            request body containing the text to be transliterated
	 * @return the transliterated text
	 */
	@RequestMapping(value = "/{languageId}/transliterate", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> transliterate(@PathVariable(value = "languageId") String languageId,
			@RequestParam(name = "addClosingVirama", defaultValue = "false") boolean addEndVirama,
			@RequestBody Map<String, Object> map) {
		String apiId = "text.transliterate";
		try {
			Request request = getRequest(map);
			Response response = dictionaryManager.transliterate(languageId, request, addEndVirama);
			TelemetryManager.log("Transliterate | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Gets the similar sound words for a given word.
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @param userId
	 *            the user id
	 * @return the similar sound words
	 */
	@RequestMapping(value = "/{languageId}/similarSound/{word:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getSimilarSoundWords(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "word") String word, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".SimilarSound";
		try {
			Response response = dictionaryManager.getSimilarSoundWords(languageId, word);
			TelemetryManager.log("Get SimilarSound | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.common.controller.BaseController#getAPIVersion()
	 */
	protected String getAPIVersion() {
		return API_VERSION_2;
	}

	/**
	 * Gets the object type of the extending object.
	 *
	 * @return the object type
	 */
	protected abstract String getObjectType();

}
