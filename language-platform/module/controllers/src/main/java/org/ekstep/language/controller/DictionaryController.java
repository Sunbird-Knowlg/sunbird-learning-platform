package org.ekstep.language.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.ilimi.graph.dac.enums.GraphDACParams;

/**
 * Entry point for all v1 CRUD operations on Word and relations.
 * 
 * @author Amarnath, Azhar, Rayulu
 */
public abstract class DictionaryController extends BaseLanguageController {

	/** The dictionary manager. */
	@Autowired
	private IDictionaryManager dictionaryManager;

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(DictionaryController.class.getName());

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
		String apiId = "media.upload";
		LOGGER.info("Upload | File: " + file);
		try {
			String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.currentTimeMillis() + "."
					+ FilenameUtils.getExtension(file.getOriginalFilename());
			File uploadedFile = new File(name);
			file.transferTo(uploadedFile);
			Response response = dictionaryManager.upload(uploadedFile);
			LOGGER.info("Upload | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Upload | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Creates the word.
	 *
	 * @param languageId
	 *            the language/graph id
	 * @param map
	 *            the word body
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".save";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.create(languageId, objectType, request);
			LOGGER.info("Create | Response: " + response);
			if (!checkError(response)) {
				List<String> nodeIds = (List<String>) response.get(GraphDACParams.node_ids.name());
				asyncUpdate(nodeIds, languageId);
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
	 * Updates the word.
	 *
	 * @param languageId
	 *            the language/graph id
	 * @param objectId
	 *            the word id
	 * @param map
	 *            the word body
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId}/{objectId:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId, @RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".update";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.update(languageId, objectId, objectType, request);
			LOGGER.info("Update | Response: " + response);
			if (!checkError(response)) {
				String nodeId = (String) response.get(GraphDACParams.node_id.name());
				asyncUpdate(nodeId, languageId);
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
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".info";
		try {
			Response response = dictionaryManager.find(languageId, objectId, fields);
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
	@RequestMapping(value = "/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAll(@PathVariable(value = "languageId") String languageId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".list";
		try {
			Response response = dictionaryManager.findAll(languageId, objectType, fields, limit);
			LOGGER.info("Find All | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Asynchronously enrich a single word.
	 *
	 * @param nodeId
	 *            the word id
	 * @param languageId
	 *            the language id
	 */
	private void asyncUpdate(String nodeId, String languageId) {
		if (StringUtils.isNotBlank(nodeId)) {
			List<String> nodeIds = new ArrayList<String>();
			nodeIds.add(nodeId);
			asyncUpdate(nodeIds, languageId);
		}
	}

	/**
	 * Asynchronously enrich multiple words.
	 *
	 * @param nodeIds
	 *            the word ids
	 * @param languageId
	 *            the language id
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
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".findWordsCSV";
		try {
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=words.csv");
			dictionaryManager.findWordsCSV(languageId, objectType, file.getInputStream(), response.getOutputStream());
			LOGGER.info("Find CSV | Response");
			response.getOutputStream().close();
			Response resp = new Response();
			return getResponseEntity(resp, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
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
	 */
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
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".relation.delete";
		try {
			Response response = dictionaryManager.deleteRelation(languageId, objectType, objectId1, relation,
					objectId2);
			LOGGER.info("Delete Relation | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
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
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{languageId}/{objectId1:.+}/{relation}/{objectId2:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> addRelation(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId1") String objectId1, @PathVariable(value = "relation") String relation,
			@PathVariable(value = "objectId2") String objectId2, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".relation.add";
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
			LOGGER.info("Add Relation | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	// TODO: Take 'objectType' from the url since it is coming from there after
	/**
	 * Gets the synonyms of a given word.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectId
	 *            the word id
	 * @param relation
	 *            the synonym relation
	 * @param fields
	 *            the fields that should be part of the result
	 * @param relations
	 *            the relations that should be part of the result
	 * @param userId
	 *            the user id
	 * @return the synonyms
	 */
	@RequestMapping(value = "/{languageId}/{relation}/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getSynonyms(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId, @PathVariable(value = "relation") String relation,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "relations", required = false) String[] relations,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".synonym.list";
		try {
			Response response = dictionaryManager.relatedObjects(languageId, objectType, objectId, relation, fields,
					relations);
			LOGGER.info("Get Synonyms | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Gets the translations of given words in the given languages.
	 *
	 * @param languageId
	 *            the language id
	 * @param words
	 *            the words
	 * @param languages
	 *            the languages
	 * @param userId
	 *            the user id
	 * @return the translations
	 */
	@RequestMapping(value = "/{languageId}/translation", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getTranslations(@PathVariable(value = "languageId") String languageId,
			@RequestParam(value = "words", required = false) String[] words,
			@RequestParam(value = "languages", required = false) String[] languages,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".word.translation";
		try {
			Response response = dictionaryManager.translation(languageId, words, languages);
			LOGGER.info("Get Translations | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
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
			LOGGER.info("loadWordsArpabetsMap | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("loadWordsArpabetsMap | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			if (null != wordsArpabetsStream)
				try {
					wordsArpabetsStream.close();
				} catch (IOException e) {
					LOGGER.error("Error! While Closing the Input Stream.", e);
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
			LOGGER.info("Get Syllables | Response: " + response);
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
			LOGGER.info("Get Arpabets | Response: " + response);
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
			LOGGER.info("Get PhoneticSpelling | Response: " + response);
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
			LOGGER.info("Transliterate | Response: " + response);
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
			LOGGER.info("Get SimilarSound | Response: " + response);
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
