package org.ekstep.language.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IDictionaryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("v1/language/indexes")
public class IndexesController extends BaseLanguageController {

    @Autowired
    private IDictionaryManager dictionaryManager;

    private static Logger LOGGER = LogManager.getLogger(IndexesController.class.getName());

    @RequestMapping(value = "/loadCitations/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> loadCitations(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "citations.load";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.loadCitations.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/citationsCount/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getCitationsCount(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "citations.count";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.citationsCount.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/citations/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getCitations(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "citations.count";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.citations.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/getRootWords/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getRootWords(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "rootWords.get";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.getRootWords.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/getWordId/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getWordId(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "wordIds.get";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.getWordId.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/getIndexInfo/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getIndexInfo(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "indexInfo.get";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.getIndexInfo.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/addWordIndex/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> addWordIndex(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "wordIndex.add";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.addWordIndex.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/addCitationIndex/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> addCitationIndex(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "citation.add";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.addCitationIndex.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/getWordMetrics/{languageId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getWordMetrics(@PathVariable(value = "languageId") String languageId,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "wordMetrics.get";
        Request request = new Request();
        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.getWordMetrics.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/wordWildCard/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> wordWildCard(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "word.wildCard";
        Request request = getRequestObject(map);
        request.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
        request.setOperation(LanguageOperations.wordWildCard.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
}
