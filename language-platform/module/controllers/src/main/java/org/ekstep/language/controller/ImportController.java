package org.ekstep.language.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IImportManager;
import org.ekstep.language.util.Constants;
import org.ekstep.language.util.ControllerUtil;
import org.ekstep.language.util.WordUtil;
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
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;

/**
 * The Class ImportController.Entry point for all import related API
 *
 * @author rayulu, amarnath and karthik
 */
@Controller
@RequestMapping("v1/language")
public class ImportController extends BaseLanguageController {

	/** The import manager. */
	@Autowired
	private IImportManager importManager;
	
	/** The controller util. */
	private ControllerUtil controllerUtil = new ControllerUtil();

	/** The word util. */
	private WordUtil wordUtil = new WordUtil();
	
	/** The logger. */
	private static  ILogger LOGGER = PlatformLogManager.getLogger();

	/**
	 * Import wordnet data from JSON.
	 *
	 * @param languageId
	 *            the language id
	 * @param zipFile
	 *            the zip file
	 * @param userId
	 *            the user id
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId:.+}/importJSON", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importJSON(@PathVariable(value = "languageId") String languageId,
			@RequestParam("zipFile") MultipartFile zipFile, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "ekstep.language.fixAssociation";
		LOGGER.log("Import | Language Id: " + languageId + " Synset File: " + zipFile + "| user-id: " + userId);
		InputStream synsetsStreamInZIP = null;
		try {
			if (null != zipFile || null != zipFile) {
				synsetsStreamInZIP = zipFile.getInputStream();
			}
			Response response = importManager.importJSON(languageId, synsetsStreamInZIP);
			LOGGER.log("Import | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Import | Exception: " ,e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			try {
				if (null != synsetsStreamInZIP)
					synsetsStreamInZIP.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.",e.getMessage(), e);
			}
		}
	}

	/**
	 * Transform data.
	 *
	 * @param languageId
	 *            the language id
	 * @param sourceId
	 *            the source id
	 * @param file
	 *            the file
	 * @param userId
	 *            the user id
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId:.+}/transform/{sourceId:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> transformData(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "sourceId") String sourceId, @RequestParam("file") MultipartFile file,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "ekstep.language.transform";
		LOGGER.log("Import | Language Id: " + languageId + " Source Id: " + sourceId + " | File: " + file
				+ " | user-id: " + userId);
		InputStream stream = null;
		try {
			if (null != file)
				stream = file.getInputStream();
			Response response = importManager.transformData(languageId, sourceId, stream);
			LOGGER.log("Import | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Import | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			try {
				if (null != stream)
					stream.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.",e.getMessage(), e);
			}
		}
	}

	/**
	 * Import synset File and word file data.
	 *
	 * @param languageId
	 *            the language id
	 * @param synsetFile
	 *            the synset file
	 * @param wordFile
	 *            the word file
	 * @param userId
	 *            the user id
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId:.+}/importData", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importData(@PathVariable(value = "languageId") String languageId,
			@RequestParam("synsetFile") MultipartFile synsetFile, @RequestParam("wordFile") MultipartFile wordFile,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "ekstep.language.fixAssociation";
		LOGGER.log("Import | Language Id: " + " | Synset File: " + synsetFile + " Synset File: " + wordFile
				+ "| user-id: " + userId);
		InputStream synsetStream = null;
		InputStream wordStream = null;
		try {
			if (null != synsetFile || null != wordFile) {
				synsetStream = synsetFile.getInputStream();
				wordStream = wordFile.getInputStream();
			}
			Response response = importManager.importData(languageId, synsetStream, wordStream);
			LOGGER.log("Import | Response: " + response);
			String csv = (String) response.getResult().get("wordList");
			if (StringUtils.isNotBlank(csv)) {
				resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
				resp.setContentType("text/csv;charset=utf-8");
				resp.setHeader("Content-Disposition", "attachment; filename=WordList.csv");
				resp.getOutputStream().write(csv.getBytes(StandardCharsets.UTF_8));
				resp.getOutputStream().close();
			}
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Import | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			try {
				if (null != synsetStream)
					synsetStream.close();
				if (null != wordStream)
					wordStream.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.",e.getMessage(), e);
			}
		}
	}

	/**
	 * Import wordnet data
	 *
	 * @param languageId
	 *            the language id
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/importwordnet/{languageId:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importwordnet(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.language.wordnet.import";
		Request request = getRequestObject(map);

		request.setManagerName(LanguageActorNames.INDOWORDNET_ACTOR.name());
		request.setOperation(LanguageOperations.importIndowordnet.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		LOGGER.log("List | Request: " + request);
		try {
			controllerUtil.makeAsyncLanguageRequest(request, LOGGER);
			Response response = new Response();
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
	 * Enrich words by updating posList, wordComplexity, lexileMeasures and
	 * frequencyCount
	 *
	 * @param languageId
	 *            the language id
	 * @param sourceId
	 *            the source id
	 * @param wordListFile
	 *            the word list file
	 * @param userId
	 *            the user id
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "/{languageId:.+}/enrich/{sourceId:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> enrich(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "sourceId") String sourceId, @RequestParam("wordListFile") MultipartFile wordListFile,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "language.enrich";
		InputStream wordListStream = null;
		try {
			if (null != wordListFile) {
				wordListStream = wordListFile.getInputStream();
			}

			Reader reader = null;
			BufferedReader br = null;
			String line;
			String[] objectDetails;
			reader = new InputStreamReader(wordListStream, "UTF8");
			br = new BufferedReader(reader);
			ArrayList<Map<String, String>> wordInfoList = new ArrayList<Map<String, String>>();
			ArrayList<String> node_ids = new ArrayList<String>();
			boolean firstLine = true;
			while ((line = br.readLine()) != null) {
				try {
					if (firstLine) {
						firstLine = false;
						continue;
					}
					objectDetails = line.split(Constants.CSV_SPLIT_BY);
					Map<String, String> wordInfo = new HashMap<String, String>();
					wordInfo.put("id", objectDetails[Constants.WL_INDEX_IDENTIFER]);
					wordInfo.put("word", objectDetails[Constants.WL_INDEX_LEMMA]);
					node_ids.add(wordInfo.get("id"));
					wordInfoList.add(wordInfo);
				} catch (ArrayIndexOutOfBoundsException e) {
					continue;
				}
			}

			addWordIndex(wordInfoList, languageId);
			enrichWords(node_ids, languageId);

			Response response = new Response();
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Import | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			try {
				if (null != wordListStream)
					wordListStream.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.",e.getMessage(), e);
			}
		}
	}

	/**
	 * Import CSV.
	 *
	 * @param id
	 *            the id
	 * @param file
	 *            the file
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "/{id:.+}/importCSV", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importCSV(@PathVariable(value = "id") String id,
			@RequestParam("file") MultipartFile file, HttpServletResponse resp) {
		String apiId = "language.importCSV";
		LOGGER.log("Create | Id: " + id + " | File: " + file);
		InputStream stream = null;
		try {
			if (null != file)
				stream = file.getInputStream();
			Response response = importManager.importCSV(id, stream);
			LOGGER.log("Create | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			try {
				if (null != stream)
					stream.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.",e.getMessage(), e);
			}
		}
	}

	/**
	 * Import definition.
	 *
	 * @param id
	 *            the id
	 * @param json
	 *            the json
	 * @return the response entity
	 */
	@RequestMapping(value = "/{id:.+}/importDefinition", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importDefinition(@PathVariable(value = "id") String id, @RequestBody String json) {
		String apiId = "definition.import";
		LOGGER.log("Import Definition | Id: " + id);
		try {
			Response response = importManager.updateDefinition(id, json);
			LOGGER.log("Import Definition | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create Definition | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Find all definitions.
	 *
	 * @param id
	 *            the id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{id:.+}/definition", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAllDefinitions(@PathVariable(value = "id") String id) {
		String apiId = "definition.list";
		LOGGER.log("Find All Definitions | Id: " + id);
		try {
			Response response = importManager.findAllDefinitions(id);
			LOGGER.log("Find All Definitions | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Find All Definitions | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * get the definition of given id
	 *
	 * @param id
	 *            the id
	 * @param objectType
	 *            the object type
	 * @param userId
	 *            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/{id:.+}/definition/{defId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findDefinition(@PathVariable(value = "id") String id,
			@PathVariable(value = "defId") String objectType, @RequestHeader(value = "user-id") String userId) {
		String apiId = "definition.find";
		LOGGER.log("Find Definition | Id: " + id + " | Object Type: " + objectType + " | user-id: " + userId);
		try {
			Response response = importManager.findDefinition(id, objectType);
			LOGGER.log("Find Definitions | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Find Definitions | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/{id:.+}/importExampleSentencesCSV", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importExampleSentecesCSV(@PathVariable(value = "id") String id,
			@RequestParam("file") MultipartFile file, HttpServletResponse resp) {
		String apiId = "language.importExampleSentencesCSV";
		LOGGER.log("Create | Id: " + id + " | File: " + file);
		InputStream stream = null;
		try {
			if (null != file)
				stream = file.getInputStream();
			List<String> wordIds = wordUtil.importExampleSentencesfor(id, stream);
			enrichWords(wordIds, id);
			Response response = new Response();
			LOGGER.log("importExampleSentecesCSV | wordList: " + wordIds.toString());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("importExampleSentecesCSV | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			try {
				if (null != stream)
					stream.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.", e.getMessage(),e);
			}
		}
	}
	
	/**
	 * Enrich words.
	 *
	 * @param node_ids
	 *            the node ids
	 * @param languageId
	 *            the language id
	 */
	private void enrichWords(List<String> node_ids, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.node_ids.name(), node_ids);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
		request.setOperation(LanguageOperations.enrichWords.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		makeAsyncRequest(request, LOGGER);
	}

	/**
	 * Adds the word index.
	 *
	 * @param wordInfoList
	 *            the word info list
	 * @param languageId
	 *            the language id
	 */
	private void addWordIndex(ArrayList<Map<String, String>> wordInfoList, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(LanguageParams.words.name(), wordInfoList);
		Request addWordIndexRequest = new Request();
		addWordIndexRequest.setRequest(map);
		addWordIndexRequest.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
		addWordIndexRequest.setOperation(LanguageOperations.addWordIndex.name());
		addWordIndexRequest.getContext().put(LanguageParams.language_id.name(), languageId);
		makeAsyncRequest(addWordIndexRequest, LOGGER);
	}
}