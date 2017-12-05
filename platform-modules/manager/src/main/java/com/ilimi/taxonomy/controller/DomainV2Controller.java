package com.ilimi.taxonomy.controller;

import java.util.ArrayList;
import java.util.List;

import org.ekstep.common.dto.Response;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Controller
@RequestMapping("/v2/domains")
public class DomainV2Controller extends BaseController {

    

    @Autowired
    private ITaxonomyManager taxonomyManager;

    @RequestMapping(value = "/graph/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "depth", required = false, defaultValue = "5") Integer depth,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "ekstep.domain.graph";
        PlatformLogger.log("domain.graph | Id: " + id + " | user-id: " + userId);
        try {
            List<String> relations = new ArrayList<String>();
            relations.add(RelationTypes.HIERARCHY.relationName());
            Response response = taxonomyManager.getSubGraph("domain", id, depth, relations);
            PlatformLogger.log("Domain Graph | Response: " , response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            PlatformLogger.log("Domain Graph | Exception: " , e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    protected String getAPIVersion() {
        return API_VERSION_2;
    }
}
