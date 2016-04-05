package org.ekstep.language.controller;

import java.io.File;
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
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.taxonomy.mgr.IAuditLogManager;

public abstract class DictionaryControllerV2 extends BaseLanguageController {

	@Autowired
	private IDictionaryManager dictionaryManager;

	@Autowired
	private IAuditLogManager auditLogManager;

	private static Logger LOGGER = LogManager.getLogger(DictionaryControllerV2.class.getName());

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

	@SuppressWarnings("unchecked")
    @RequestMapping(value = "/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".save";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.createWordV2(languageId, objectType, request);
			LOGGER.info("Create | Response: " + response);
			if (!checkError(response)) {
			    List<String> nodeIds = (List<String>) response.get(GraphDACParams.node_ids.name());
			    asyncUpdate(nodeIds, languageId);
			    AuditRecord audit = new AuditRecord(languageId, null, "CREATE", response.getParams(), userId,
	                    map.get("request").toString(), (String) map.get("COMMENT"));
	            auditLogManager.saveAuditRecord(audit);
			}
			
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{languageId}/{objectId:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId, @RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".update";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.updateWordV2(languageId, objectId, objectType, request);
			LOGGER.info("Update | Response: " + response);
			if (!checkError(response)) {
				List<String> nodeIds = (List<String>) response.get(GraphDACParams.node_ids.name());
			    asyncUpdate(nodeIds, languageId);
			    AuditRecord audit = new AuditRecord(languageId, null, "UPDATE", response.getParams(), userId,
	                    map.get("request").toString(), (String) map.get("COMMENT"));
	            auditLogManager.saveAuditRecord(audit);
			}
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}
	
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
	
	@RequestMapping(value = "/findByCSV/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> findWordsCSV(@PathVariable(value = "languageId") String languageId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "user-id") String userId, HttpServletResponse response) {
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
					finalMessage = finalMessage + ", "+ message;
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
	// dictionary
	// "GET -
	// v1/language/dictionary/word/{languageId}/synonym/{wordId}?fields={fields}"
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

	/*private Request getRequestObject(Map<String, Object> requestMap, String objectType) {
		Request request = getRequest(requestMap);
		Map<String, Object> map = request.getRequest();
		ObjectMapper mapper = new ObjectMapper();
		if (null != map && !map.isEmpty()) {
			Object obj = map.get(objectType.toLowerCase().trim());
			if (null != obj) {
				Node content = (Node) mapper.convertValue(obj, Node.class);
				request.put(objectType.toLowerCase().trim(), content);
			}
		}
		return request;
	}*/

	protected String getAPIVersion() {
        return API_VERSION_2;
    }
	
	protected abstract String getObjectType();

}
