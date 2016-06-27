package org.ekstep.language.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("v2/language/wordchain")
public class WordChainsController extends BaseLanguageController {

    private static Logger LOGGER = LogManager.getLogger(WordChainsController.class.getName());

    @RequestMapping(value = "/search/{languageId}/{ruleId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getComplexity(@RequestBody Map<String, Object> map) {
        String apiId = "wordchain.get";
        Request request = getRequest(map);
        String language = (String) request.get(LanguageParams.language_id.name());
        // TODO: return error response if language value is blank
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), language);
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
    
    
    @RequestMapping(value = "/complexityMeasures/text", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> computeTextComplexity(@RequestBody Map<String, Object> map) {
        String apiId = "text.complexity";
        Request request = getRequest(map);
        String language = (String) request.get(LanguageParams.language_id.name());
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeTextComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), language);
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
    
    @RequestMapping(value = "/text/analysis", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> analyseTextsCSV(@RequestBody Map<String, Object> map) {
        String apiId = "text.analysis.csv";
        Request request = getRequest(map);
        String language = (String) request.get(LanguageParams.language_id.name());
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.analyseTextsCSV.name());
        request.getContext().put(LanguageParams.language_id.name(), language);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getBulkOperationResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/textAnalysis", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> analyseTexts(@RequestBody Map<String, Object> map) {
        String apiId = "text.analysis";
        Request request = getRequest(map);
        String language = (String) request.get(LanguageParams.language_id.name());
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.analyseTexts.name());
        request.getContext().put(LanguageParams.language_id.name(), language);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getBulkOperationResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/wordComplexity/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> computeWordComplexityV2(@PathVariable(value = "languageId") String languageId, @RequestBody Map<String, Object> map) {
        String apiId = "word.complexity";
        Request request = getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.getWordComplexities.name());
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
    
    @RequestMapping(value = "/syncDefinition/{definitionName}/{languageId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> syncDefinition(@PathVariable(value = "languageId") String languageId, @PathVariable(value = "definitionName") String definitionName) {
        String apiId = "word.complexity";
        Request request = new Request();
        request.put(LanguageParams.definitionName.name(), definitionName);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.syncDefinition.name());
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
