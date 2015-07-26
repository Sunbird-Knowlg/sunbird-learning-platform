package com.ilimi.assessment.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
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

import com.ilimi.assessment.enums.AssessmentAPIParams;
import com.ilimi.assessment.mgr.IAssessmentManager;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

/**
 * 
 * @author mahesh
 *
 */

@Controller
@RequestMapping("/assessmentitemset")
public class AssessmentItemSetController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(AssessmentItemSetController.class.getName());
    
    @Autowired
    private IAssessmentManager assessmentManager;
    
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "assessment_item_set.create";
        Request request = getRequestObject(map);
        LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = assessmentManager.createItemSet(taxonomyId, request);
            LOGGER.info("Create | Response: " + response);
            return getResponseEntity(response, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "isfields", required = false) String[] isfields, @RequestHeader(value = "user-id") String userId) {
        
        String apiId = "assessment_item.find";
        LOGGER.info("Find Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + isfields + " | user-id: " + userId);
        try {
            Response response = assessmentManager.getItemSet(id, taxonomyId, isfields);
            LOGGER.info("Find Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = request.getRequest();
        if (null != map && !map.isEmpty()) {
            try {
                Object objItemSet = map.get(AssessmentAPIParams.assessment_item_set.name());
                if (null != objItemSet) {
                    Node itemSetNode = (Node) mapper.convertValue(objItemSet, Node.class);
                    request.put(AssessmentAPIParams.assessment_item_set.name(), itemSetNode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }
}
