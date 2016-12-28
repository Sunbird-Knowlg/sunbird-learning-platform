package com.ilimi.assessment.controller;

import java.util.Map;

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

import com.ilimi.assessment.dto.ItemSetSearchCriteria;
import com.ilimi.assessment.enums.AssessmentAPIParams;
import com.ilimi.assessment.enums.AssessmentErrorCodes;
import com.ilimi.assessment.mgr.IAssessmentManager;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.graph.dac.model.Node;

/**
 * 
 * @author mahesh
 *
 */

@Controller
@RequestMapping("/v3")
public class AssessmentItemSetV3Controller extends BaseController {

    private static LogHelper LOGGER = LogHelper.getInstance(AssessmentItemSetV3Controller.class.getName());

    private static final String V2_GRAPH_ID = "domain";

    @Autowired
    private IAssessmentManager assessmentManager;

    @RequestMapping(value = "/create/itemsets", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item_set.create";
        Request request = getRequestObject(map);
        LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = assessmentManager.createItemSet(taxonomyId, request);
            LOGGER.info("Create | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/generate/itemsets/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> generate(@PathVariable(value = "id") String id,
            @RequestParam(value = "isfields", required = false) String[] isfields,
            @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item_set.find";
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + isfields + " | user-id: "
                + userId);
        try {
            Response response = assessmentManager.getItemSet(id, taxonomyId, isfields, true);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
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
