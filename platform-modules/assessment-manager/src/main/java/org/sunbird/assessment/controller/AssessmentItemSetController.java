package org.sunbird.assessment.controller;

import java.util.Map;

import org.sunbird.assessment.dto.ItemSetSearchCriteria;
import org.sunbird.assessment.enums.AssessmentAPIParams;
import org.sunbird.assessment.enums.AssessmentErrorCodes;
import org.sunbird.assessment.mgr.IAssessmentManager;
import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.telemetry.logger.TelemetryManager;
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

/**
 * 
 * @author mahesh
 *
 */

@Controller
@RequestMapping("/v1/assessmentitemset")
public class AssessmentItemSetController extends BaseController {

    private static final String V2_GRAPH_ID = "domain";

    @Autowired
    private IAssessmentManager assessmentManager;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.create";
        Request request = getRequestObject(map);
        TelemetryManager.log("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = assessmentManager.createItemSet(taxonomyId, request);
            TelemetryManager.log("Create | Response: " , response.getResult());
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            TelemetryManager.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.update";
        Request request = getRequestObject(map);
        TelemetryManager.log("Update Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request
                + " | user-id: " + userId);
        try {
            Response response = assessmentManager.updateItemSet(id, taxonomyId, request);
            TelemetryManager.log("Update | Response: " , response.getResult());
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            TelemetryManager.error("Update | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "isfields", required = false) String[] isfields,
            @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.info";
        TelemetryManager.log("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + isfields + " | user-id: "
                + userId);
        try {
            Response response = assessmentManager.getItemSet(id, taxonomyId, isfields, false);
            TelemetryManager.log("Find | Response: " , response.getResult());
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/generate/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> generate(@PathVariable(value = "id") String id,
            @RequestParam(value = "isfields", required = false) String[] isfields,
            @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.generate";
        TelemetryManager.log("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + isfields + " | user-id: "
                + userId);
        try {
            Response response = assessmentManager.getItemSet(id, taxonomyId, isfields, true);
            TelemetryManager.log("Find | Response: " , response.getResult());
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.search";
        TelemetryManager.log("Search | TaxonomyId: " + taxonomyId + " | user-id: " + userId);
        try {
            Request reqeust = getSearchRequest(map);
            Response response = assessmentManager.searchItemSets(taxonomyId, reqeust);
            TelemetryManager.log("Search | Response: " , response.getResult());
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Search | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
            @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.delete";
        TelemetryManager.log("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = assessmentManager.deleteItemSet(id, taxonomyId);
            TelemetryManager.log("Delete | Response: " , response.getResult());
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Delete | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    private Request getSearchRequest(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        if (null != map && !map.isEmpty()) {
            try {
                ItemSetSearchCriteria criteria = mapper.convertValue(map, ItemSetSearchCriteria.class);
                request.put(AssessmentAPIParams.assessment_search_criteria.name(), criteria);
            } catch (Exception e) {
                throw new MiddlewareException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_SEARCH_CRITERIA.name(),
                        "Invalid search criteria.", e);
            }
        } else if (null != map && map.isEmpty()) {
            request.put(AssessmentAPIParams.assessment_search_criteria.name(), new ItemSetSearchCriteria());
        }
        return request;
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
