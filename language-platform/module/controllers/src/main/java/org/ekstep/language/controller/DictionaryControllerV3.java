package org.ekstep.language.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.mgr.IDictionaryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Response;


/**
 * Entry point for all v3 CRUD operations on Word and relations.
 *
 * @author karthik
 */
public abstract class DictionaryControllerV3 extends BaseLanguageController {

	/** The word controller. */
	@Autowired
	private WordControllerV2 wordController;

	/** The dictionary manager. */
	@Autowired
	private IDictionaryManager dictionaryManager;
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(DictionaryControllerV3.class.getName());
	
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
	@RequestMapping(value = "/read/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> find(@RequestParam(value = "language_id", required = true) String languageId,
			@PathVariable(value = "objectId") String objectId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".info";
		try {
			Response response = dictionaryManager.getWordV3(languageId, objectId);
			LOGGER.info("Find | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Find | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
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
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAll(@RequestParam(value = "language_id", required = true) String languageId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestHeader(value = "user-id") String userId) {
		return wordController.findAll(languageId, fields, limit, userId, API_VERSION_3);
	}

	/**
	 * Creates word by processing primary meaning and related words.
	 *
	 * @param languageId
	 *            Graph Id
	 * @param forceUpdate
	 *            Indicates if the update should be forced
	 * @param map
	 *            Request map
	 * @param userId
	 *            User making the request
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestParam(value = "language_id", required = true) String languageId,
			@RequestParam(name = "force", required = false, defaultValue = "false") boolean forceUpdate,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		return wordController.create(languageId, forceUpdate, map, userId);
	}

	/**
	 * Updates word partially by updating only what new has come in the request.
	 *
	 * @param languageId
	 *            Graph Id
	 * @param objectId
	 *            Id of the object that needs to be updated
	 * @param map
	 *            Request map
	 * @param forceUpdate
	 *            Indicates if the update should be forced
	 * @param userId
	 *            User making the request
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{objectId:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> partialUpdate(@RequestParam(value = "language_id", required = true) String languageId,
			@PathVariable(value = "objectId") String objectId, @RequestBody Map<String, Object> map,
			@RequestParam(name = "force", required = false, defaultValue = "false") boolean forceUpdate,
			@RequestHeader(value = "user-id") String userId) {
		return wordController.update(languageId, objectId, map, forceUpdate, userId);
	}

	/**
	 * Gets the object type of the extending object.
	 *
	 * @return the object type
	 */
	protected abstract String getObjectType();
}
