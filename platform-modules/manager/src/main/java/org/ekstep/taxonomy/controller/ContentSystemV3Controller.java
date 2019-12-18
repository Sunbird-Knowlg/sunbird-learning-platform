package org.ekstep.taxonomy.controller;

import java.util.Map;

import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/system/v3")
public class ContentSystemV3Controller extends BaseController {

    @Autowired
    private IContentManager contentManager;

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/content/update/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String contentId,
                                                         @RequestBody Map<String, Object> requestMap) {
        String apiId = "ekstep.learning.system.content.update";
        Request request = getRequest(requestMap);
        try {
            Map<String, Object> map = (Map<String, Object>) request.get("content");
            Response response = contentManager.updateAllContents(contentId, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}