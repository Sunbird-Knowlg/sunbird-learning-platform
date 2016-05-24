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

import com.ilimi.assessment.controller.AssessmentItemController;
import com.ilimi.assessment.mgr.IAssessmentManager;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.IAuditHistoryManager;

@Controller
@RequestMapping("/v1/audit")
public class AuditHistoryController extends BaseController{

    private static Logger LOGGER = LogManager.getLogger(AssessmentItemController.class.getName());

    @Autowired
    private IAuditHistoryManager auditHistoryManager;

    @RequestMapping(value = {"/{graphId:.+}/all/{startTime:.+}/{endTime:.+}","/{graphId:.+}/all/{startTime:.+}/"}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getAll(@PathVariable Map<String, String> pathVariables,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "audit_history.getAll";
        String graphId, startTime, endTime;		

		graphId=pathVariables.get("graphId");
    	startTime=pathVariables.get("startTime");
    	if(pathVariables.containsKey("endTime")){
    		endTime=pathVariables.get("endTime");
    	}else{
    		endTime=null;
    	}
        
        LOGGER.info("get all AuditHistory | GraphId: " + graphId + " | TimeStamp1: " + startTime + " | Timestamp2: " + endTime);
        try {
            Response response = auditHistoryManager.getAuditHistory(graphId, startTime, endTime);
            LOGGER.info("Find Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = {"/history/{graphId:.+}/{objectId:.+}/{startTime:.+}/{endTime:.+}","/history/{graphId:.+}/{objectId:.+}/{startTime:.+}/"}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getById(@PathVariable Map<String, String> pathVariables,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "audit_history.getAll";
        
        String graphId, objectId, startTime, endTime;		

		graphId=pathVariables.get("graphId");
		objectId=pathVariables.get("objectId");
    	startTime=pathVariables.get("startTime");
    	if(pathVariables.containsKey("endTime")){
    		endTime=pathVariables.get("endTime");
    	}else{
    		endTime=null;
    	}
    	LOGGER.info("get AuditHistory By ObjectId | GraphId: " + graphId + " | TimeStamp1: " + startTime + " | Timestamp2: " + endTime + " | ObjectId: "+objectId);
        try {
            Response response = auditHistoryManager.getAuditHistoryById(graphId, objectId, startTime, endTime);
            LOGGER.info("Find Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = {"/{graphId:.+}/{objectType:.+}/{startTime:.+}/{endTime:.+}","/{graphId:.+}/{objectType:.+}/{startTime:.+}/"}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getByType(@PathVariable Map<String, String> pathVariables,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "audit_history.getAll";
        
        String graphId, objectType, startTime, endTime;		

		graphId=pathVariables.get("graphId");
		objectType=pathVariables.get("objectType");
    	startTime=pathVariables.get("startTime");
    	if(pathVariables.containsKey("endTime")){
    		endTime=pathVariables.get("endTime");
    	}else{
    		endTime=null;
    	}
        LOGGER.info("get AuditHistory By ObjectType | GraphId: " + graphId + " | TimeStamp1: " + startTime + " | Timestamp2: " + endTime + " | ObjectType: "+objectType);
        try {
            Response response = auditHistoryManager.getAuditHistoryByType(graphId, objectType, startTime, endTime);
            LOGGER.info("Find Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
}
