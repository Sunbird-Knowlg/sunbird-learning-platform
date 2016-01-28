package com.ilimi.taxonomy.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.IContentManager;

@Controller
@RequestMapping("/v2/content")
public class ContentV2Controller extends BaseController {
	private static Logger LOGGER = LogManager.getLogger(ContentV2Controller.class.getName());
    @Autowired
    private ContentController contentController;
    
    @Autowired
    private IContentManager contentManager;
    

    @RequestMapping(value = "/upload/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> upload(@PathVariable(value = "id") String id,
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestHeader(value = "user-id") String userId) {
        return contentController.upload(id, file, "domain", userId, null);
    }
    
    @RequestMapping(value = "/publish", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> publish(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
    		@RequestParam(value = "contentId", required = true)  String contentId) {
    	  String apiId = "content.publish";
    	  LOGGER.info("getParseContent has Taxonomy Id :: " + taxonomyId + "Content Id : " + contentId );
    	  Response response = contentManager.getParseContent(taxonomyId, contentId);
        return getResponseEntity(response, apiId, null);
    }
    @RequestMapping(value = "/extract", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> extract(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
    		@RequestParam(value = "contentId", required = true)  String contentId) {
    	  String apiId = "content.extract";
    	  LOGGER.info("getExtractContent has Taxonomy Id :: " + taxonomyId + "Content Id : " + contentId );
    	  Response response = contentManager.getExtractContent(taxonomyId, contentId);
        return getResponseEntity(response, apiId, null);
    }
}
