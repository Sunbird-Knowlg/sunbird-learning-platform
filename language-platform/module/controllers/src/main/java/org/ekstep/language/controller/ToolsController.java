package org.ekstep.language.controller;

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

    @RequestMapping(value = "/lexileMeasures", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getComplexity(@RequestBody Map<String, Object> map) {
        String apiId = "language.complexity";
        apiId = "language.complexity";
        Request request = getRequestObject(map);

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
}
