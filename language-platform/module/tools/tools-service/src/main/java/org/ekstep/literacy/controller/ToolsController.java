package org.ekstep.literacy.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.LanguageMap;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.ContentAPIParams;

@Controller
@RequestMapping("v1/language/tools")
public class ToolsController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(ToolsController.class.getName());

    @RequestMapping(value = "/lexileMeasures", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> complexity(
            @RequestParam(value = "languageId", required = true, defaultValue = "") String languageId,
            @RequestBody Map<String, Object> map) {
        String apiId = "language.complexity";
        if (LanguageMap.containsLanguage(languageId)) {
            apiId = "language.complexity";
            Request request = getRequestObject(map);
            LOGGER.info("List | Request: " + request);
            try {
                Response response = null;
                LOGGER.info("List | Response: " + response);
                return getResponseEntity(response, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            } catch (Exception e) {
                LOGGER.error("List | Exception: " + e.getMessage(), e);
                return getExceptionResponseEntity(e, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            }
        } else {
            return getExceptionResponseEntity(new ClientException("ERR_INVALID_LANGUAGE_ID", "Language ID is invalid."),
                    apiId, null);
        }
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private Request getListRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        if (null != requestMap && !requestMap.isEmpty()) {
            Object requestObj = requestMap.get("request");
            if (null != requestObj) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> map = mapper.readValue(strRequest, Map.class);
                    request.setRequest(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return request;
    }

    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        ObjectMapper mapper = new ObjectMapper();
        if (null != map && !map.isEmpty()) {
            try {
                Object obj = map.get(ContentAPIParams.content.name());
                if (null != obj) {
                    Node content = (Node) mapper.convertValue(obj, Node.class);
                    request.put(ContentAPIParams.content.name(), content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }
}
