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

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.IAuditLogManager;

@Controller
@RequestMapping("/audit")
public class AuditController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(AuditController.class.getName());
    
    @Autowired
    IAuditLogManager auditLogManager;
    
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getAuditHistory(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        String apiId = "audit.list";
        LOGGER.info("AuditHistory | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = auditLogManager.getAuditHistory(taxonomyId, id);
            LOGGER.info("AuditHistory | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("AuditHistory | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
