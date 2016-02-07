package com.ilimi.taxonomy.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;

@Controller
@RequestMapping("/v2/content")
public class ContentV2Controller extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(ContentV2Controller.class.getName());

    @Autowired
    private ContentController contentController;

    @Autowired
    private IContentManager contentManager;

    private String graphId = "domain";

    @RequestMapping(value = "/upload/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> upload(@PathVariable(value = "id") String id,
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestHeader(value = "user-id") String userId) {
        return contentController.upload(id, file, "domain", userId, null);
    }

    @RequestMapping(value = "/publish/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> publish(@PathVariable(value = "id") String contentId,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "content.publish";
        LOGGER.info("Publish content | Content Id : " + contentId);
        try {
            Response response = contentManager.publish(graphId, contentId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Publish | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/extract/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> extract(@PathVariable(value = "id") String contentId,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "content.extract";
        LOGGER.info("Extract content | Content Id : " + contentId);
        try {
            Response response = contentManager.extract(graphId, contentId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Extract | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/bundle", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> bundle(@RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "content.archive";
        LOGGER.info("Create Content Bundle | user-id: " + userId);
        try {
            Request request = getBundleRequest(map, ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name());
            Response response = contentManager.bundle(request, graphId, "1.1");
            LOGGER.info("Archive | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Archive | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
