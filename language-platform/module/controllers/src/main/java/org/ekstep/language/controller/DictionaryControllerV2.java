
package org.ekstep.language.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
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
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.GraphDACParams;

/**
 * End points for v2 word CRUD operations and relation CRUD operations. V2
 * Handles primary meaning and related words
 * 
 * @author Amarnath
 */
public abstract class DictionaryControllerV2 extends BaseLanguageController {

	@Autowired
	private IDictionaryManager dictionaryManager;

	@Autowired
	private WordController wordController;

	private static Logger LOGGER = LogManager.getLogger(DictionaryControllerV2.class.getName());

	@RequestMapping(value = "/media/upload", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> upload(@RequestParam(value = "file", required = true) MultipartFile file) {
		return wordController.upload(file);
	}

	/**
	 * Creates word by processing primary meaning and related words
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param forceUpdate
	 *            Indicates if the update should be forced
	 * @param map
	 *            Request map
	 * @param userId
	 *            User making the request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "languageId") String languageId,
			@RequestParam(name = "force", required = false, defaultValue = "false") boolean forceUpdate,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".save";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.createWordV2(languageId, objectType, request, forceUpdate);
			LOGGER.info("Create | Response: " + response);
			if (!checkError(response)) {
				String nodeId = (String) response.get(GraphDACParams.node_id.name());
				List<String> nodeIds = (List<String>) response.get(GraphDACParams.node_ids.name());
				asyncUpdate(nodeIds, languageId);
				response.getResult().remove(GraphDACParams.node_ids.name());
				response.getResult().remove(GraphDACParams.node_id.name());
				if (StringUtils.isNotBlank(nodeId))
					response.put(GraphDACParams.node_ids.name(), Arrays.asList(nodeId));
			} else {
				response.getResult().remove(GraphDACParams.node_ids.name());
				response.getResult().remove(GraphDACParams.node_id.name());
			}
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Updates word by processing primary meaning and related words
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
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{languageId}/{objectId:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId, @RequestBody Map<String, Object> map,
			@RequestParam(name = "force", required = false, defaultValue = "false") boolean forceUpdate,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".update";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.updateWordV2(languageId, objectId, objectType, request, forceUpdate);
			LOGGER.info("Update | Response: " + response);
			if (!checkError(response)) {
				String nodeId = (String) response.get(GraphDACParams.node_id.name());
				List<String> nodeIds = (List<String>) response.get(GraphDACParams.node_ids.name());
				asyncUpdate(nodeIds, languageId);
				response.getResult().remove(GraphDACParams.node_ids.name());
				response.getResult().remove(GraphDACParams.node_id.name());
				if (StringUtils.isNotBlank(nodeId))
					response.put(GraphDACParams.node_ids.name(), Arrays.asList(nodeId));
			} else {
				response.getResult().remove(GraphDACParams.node_ids.name());
				response.getResult().remove(GraphDACParams.node_id.name());
			}
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Makes an Async call to Enrich actor to enrich the words
	 * 
	 * @param nodeIds
	 *            List of word Ids that has to be enriched
	 * @param languageId
	 *            Graph Id
	 */
	private void asyncUpdate(List<String> nodeIds, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.node_ids.name(), nodeIds);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
		request.setOperation(LanguageOperations.enrichWords.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		makeAsyncRequest(request, LOGGER);
	}

	/**
	 * Searches for a given word using the object Id with its primary meaning
	 * and related words
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param objectId
	 *            Id of the word that needs to be searched
	 * @param fields
	 *            List of fields that should be part of the result
	 * @param userId
	 *            User making the request
	 * @return
	 */
	@RequestMapping(value = "/{languageId}/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> find(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".info";
		try {
			Response response = dictionaryManager.findV2(languageId, objectId, fields);
			LOGGER.info("Find | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Find | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Get all words with their primary meanings and related words
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param fields
	 *            List of fields that should be part of the result
	 * @param limit
	 *            Limit of results
	 * @param userId
	 *            User making the request
	 * @return
	 */
	@RequestMapping(value = "/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAll(@PathVariable(value = "languageId") String languageId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".list";
		try {
			Response response = dictionaryManager.findAllV2(languageId, objectType, fields, limit);
			LOGGER.info("Find All | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Retrieve words from the lemma in the CSV
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param file
	 *            Input CSV with list of lemmas
	 * @param userId
	 *            User making the request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/findByCSV/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> findWordsCSV(@PathVariable(value = "languageId") String languageId,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse response) {
		return wordController.findWordsCSV(languageId, file, userId, response);
	}

	/**
	 * Delete a given relation
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param objectId1
	 *            Start object
	 * @param relation
	 *            Relation name
	 * @param objectId2
	 *            End object
	 * @param userId
	 *            User making the request
	 * @return
	 */
	@RequestMapping(value = "/{languageId}/{objectId1:.+}/{relation}/{objectId2:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> deleteRelation(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId1") String objectId1, @PathVariable(value = "relation") String relation,
			@PathVariable(value = "objectId2") String objectId2, @RequestHeader(value = "user-id") String userId) {
		return wordController.deleteRelation(languageId, objectId1, relation, objectId2, userId);
	}

	/**
	 * Creates a relation between two objects
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param objectId1
	 *            Start object
	 * @param relation
	 *            Relation name
	 * @param objectId2
	 *            End object
	 * @param userId
	 *            User making the request
	 * @return
	 */
	@RequestMapping(value = "/{languageId}/{objectId1:.+}/{relation}/{objectId2:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> addRelation(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId1") String objectId1, @PathVariable(value = "relation") String relation,
			@PathVariable(value = "objectId2") String objectId2, @RequestHeader(value = "user-id") String userId) {
		return wordController.addRelation(languageId, objectId1, relation, objectId2, userId);
	}

	/**
	 * Gets the synonyms of a given word
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param objectId
	 *            word Id
	 * @param relation
	 *            relation of the word
	 * @param fields
	 *            List of fields in the response
	 * @param relations
	 * @param userId
	 *            User making the request
	 * @return
	 */
	@RequestMapping(value = "/{languageId}/{relation}/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getSynonyms(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId, @PathVariable(value = "relation") String relation,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "relations", required = false) String[] relations,
			@RequestHeader(value = "user-id") String userId) {
		return wordController.getSynonyms(languageId, objectId, relation, fields, relations, userId);
	}

	/**
	 * Get translations of the given words in other languages
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param words
	 *            List of lemmas
	 * @param languages
	 *            List of languages
	 * @param userId
	 *            User making the request
	 * @return
	 */
	@RequestMapping(value = "/{languageId}/translation", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getTranslations(@PathVariable(value = "languageId") String languageId,
			@RequestParam(value = "words", required = false) String[] words,
			@RequestParam(value = "languages", required = false) String[] languages,
			@RequestHeader(value = "user-id") String userId) {
		return wordController.getTranslations(languageId, words, languages, userId);
	}

	protected String getAPIVersion() {
		return API_VERSION_2;
	}

	protected abstract String getObjectType();

}
