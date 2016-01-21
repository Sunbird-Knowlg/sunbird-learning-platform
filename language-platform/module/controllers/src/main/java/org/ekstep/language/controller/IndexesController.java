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
        String apiId = "citations.load";
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

}
