
package org.ekstep.language.controller;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
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
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;

/**
 * End points for v2 word CRUD operations and relation CRUD operations. V2
 * implementation Handles primary meaning and related words
 * 
 * @author Amarnath
 */
public abstract class DictionaryControllerV2 extends BaseLanguageController {

	/** The dictionary manager. */
	@Autowired
	private IDictionaryManager dictionaryManager;

	/** The logger. */
	private static ILogger LOGGER = new PlatformLogger(DictionaryControllerV2.class.getName());

	/**
	 * Upload.
	 *
	 * @param file
	 *            the file
	 * @return the response entity
	 */
	@RequestMapping(value = "/media/upload", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> upload(@RequestParam(value = "file", required = true) MultipartFile file) {
		String apiId = "media.upload";
		LOGGER.log("Upload | File: " + file);
		try {
			String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.currentTimeMillis() + "."
					+ FilenameUtils.getExtension(file.getOriginalFilename());
			File uploadedFile = new File(name);
			file.transferTo(uploadedFile);
			Response response = dictionaryManager.upload(uploadedFile);
			LOGGER.log("Upload | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Upload | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
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
			LOGGER.log("Create | Response: " + response);
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
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Updates word partially by updating only what new has come in the request
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
			LOGGER.log("Update | Response: " + response);
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
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	/**
	 * Makes an Async call to Enrich actor to enrich the words.
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
	 * and related words.
	 *
	 * @param languageId
	 *            Graph Id
	 * @param objectId
	 *            Id of the word that needs to be searched
	 * @param fields
	 *            List of fields that should be part of the result
	 * @param userId
	 *            User making the request
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> find(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestHeader(value = "user-id") String userId,
			@RequestParam(value = "version", required = false, defaultValue = API_VERSION_2) String version) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".info";
		try {
			Response response = dictionaryManager.find(languageId, objectId, fields, version);
			LOGGER.log("Find | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Find | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Get all words with their primary meanings and related words.
	 *
	 * @param languageId
	 *            Graph Id
	 * @param fields
	 *            List of fields that should be part of the result
	 * @param limit
	 *            Limit of results
	 * @param userId
	 *            User making the request
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAll(@PathVariable(value = "languageId") String languageId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestHeader(value = "user-id") String userId,
			@RequestParam(value = "version", required = false, defaultValue = API_VERSION_2) String version) {
		String objectType = getObjectType();
		String apiId = "ekstep.language." +objectType.toLowerCase() + ".list";
		try {
			Response response = dictionaryManager.findAll(languageId, objectType, fields, limit, version);
			LOGGER.log("Find All | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Retrieve words from the lemma in the CSV.
	 *
	 * @param languageId
	 *            Graph Id
	 * @param file
	 *            Input CSV with list of lemmas
	 * @param userId
	 *            User making the request
	 * @param response
	 *            the response
	 * @return the response entity
	 */
	@RequestMapping(value = "/findByCSV/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> findWordsCSV(@PathVariable(value = "languageId") String languageId,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse response) {
		String objectType = getObjectType();
		String apiId = "ekstep.language."+objectType.toLowerCase() + ".findWordsCSV";
		try {
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=words.csv");
			dictionaryManager.findWordsCSV(languageId, objectType, file.getInputStream(), response.getOutputStream());
			LOGGER.log("Find CSV | Response");
			response.getOutputStream().close();
			Response resp = new Response();
			return getResponseEntity(resp, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Delete a given relation.
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
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/{objectId1:.+}/{relation}/{objectId2:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> deleteRelation(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId1") String objectId1, @PathVariable(value = "relation") String relation,
			@PathVariable(value = "objectId2") String objectId2, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = "ekstep.language."+objectType.toLowerCase() + ".relation.delete";
		try {
			Response response = dictionaryManager.deleteRelation(languageId, objectType, objectId1, relation,
					objectId2);
			LOGGER.log("Delete Relation | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Creates a relation between two objects.
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
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{languageId}/{objectId1:.+}/{relation}/{objectId2:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> addRelation(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId1") String objectId1, @PathVariable(value = "relation") String relation,
			@PathVariable(value = "objectId2") String objectId2, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = "ekstep.language."+objectType.toLowerCase() + ".relation.add";
		try {
			Response response = dictionaryManager.addRelation(languageId, objectType, objectId1, relation, objectId2);
			List<String> messages = (List<String>) response.get("messages");
			if (messages != null) {
				String finalMessage = "";
				for (String message : messages) {
					finalMessage = finalMessage + ", " + message;
				}
				throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(), finalMessage.substring(2));
			}
			LOGGER.log("Add Relation | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.common.controller.BaseController#getAPIVersion()
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
