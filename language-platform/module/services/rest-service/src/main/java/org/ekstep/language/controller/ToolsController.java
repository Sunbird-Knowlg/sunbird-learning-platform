package org.ekstep.language.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("v1/language/tools")
public class ToolsController extends BaseLanguageController {

    private static Logger LOGGER = LogManager.getLogger(ToolsController.class.getName());

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/lexileMeasures", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> complexity(@RequestBody Map<String, Object> map) {
        String apiId = "language.complexity";
        apiId = "language.complexity";
        Request request = getRequestObject(map);
        String language = (String) request.get(LanguageParams.language_id.name());
        // TODO: throw an exception is language value is blank
        
        request.getContext().put(LanguageParams.language_id.name(), language);
        LOGGER.info("List | Request: " + request);
        try {
            List<Request> requests = new ArrayList<Request>();
            List<String> words = (List<String>) request.get(LanguageParams.words.name());
            if (null != words && words.size() > 0) {
                for (String word : words) {
                    Request req = new Request(request);
                    req.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
                    req.setOperation(LanguageOperations.getWordComplexity.name());
                    req.put(LanguageParams.language_id.name(), language);
                    req.put(LanguageParams.word.name(), word);
                    requests.add(req);
                }
            }
            
            List<String> texts = (List<String>) request.get(LanguageParams.texts.name());
            if (null != texts && texts.size() > 0) {
                for (String text : texts) {
                    Request req = new Request(request);
                    req.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
                    req.setOperation(LanguageOperations.getTextComplexity.name());
                    req.put(LanguageParams.language_id.name(), language);
                    req.put(LanguageParams.text.name(), text);
                    requests.add(req);
                }
            }
            
            // TODO: send all requests to request router and get the response
            Response response = null;
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
