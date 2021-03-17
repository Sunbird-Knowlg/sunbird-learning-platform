package org.sunbird.taxonomy.controller;

import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.taxonomy.mgr.IContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller Class for All CRUD Operation on Collection
 *
 * @author Kumar Gauraw
 */
@Controller
@RequestMapping("/collection/v3")
public class CollectionV3controller extends BaseController {

    @Autowired
    private IContentManager contentManager;

    /**
     *
     * @param contentId
     * @param requestMap
     * @param channelId
     * @return
     */
    @RequestMapping(value = "/dialcode/link/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> linkDialCode(@PathVariable(value = "id") String contentId,
                                                 @RequestBody Map<String, Object> requestMap,
                                                 @RequestHeader(value = "X-Channel-Id", required = true) String channelId) {
        String apiId = "ekstep.collection.dialcode.link";
        Request request = getRequest(requestMap);
        try {
            Object reqObj = request.get("content");
            Response response = contentManager.linkDialCode(channelId, reqObj, "collection", contentId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception occured while Linking Dial Code with Content: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
