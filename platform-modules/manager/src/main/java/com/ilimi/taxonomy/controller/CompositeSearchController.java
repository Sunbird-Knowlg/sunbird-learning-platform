package com.ilimi.taxonomy.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Controller
@RequestMapping("v2/search")
public class CompositeSearchController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(CompositeSearchController.class.getName());

    @Autowired
    private ITaxonomyManager taxonomyManager;


    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search() {
        String apiId = "ekstep.composite.search";
        LOGGER.info("ekstep.composite.search");
        try {
            Response response = taxonomyManager.compositeSearch();
            LOGGER.info("Composite Search | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Composite Search | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    
}
