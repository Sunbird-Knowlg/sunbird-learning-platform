package org.ekstep.assessment.controller;

import java.util.Map;

import org.ekstep.assessment.dto.ItemSetSearchCriteria;
import org.ekstep.assessment.enums.AssessmentAPIParams;
import org.ekstep.assessment.enums.AssessmentErrorCodes;
import org.ekstep.assessment.mgr.IAssessmentManager;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/assessment/v3/itemsets")
public class AssessmentItemSetV3Controller extends BaseController {

    

    private static final String V2_GRAPH_ID = "domain";

    @Autowired
    private IAssessmentManager assessmentManager;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.create";
        Request request = getRequestObject(map);
        TelemetryManager.log("Create | TaxonomyId: " + taxonomyId + " | Request: " + request);
        try {
            Response response = assessmentManager.createItemSet(taxonomyId, request);
            TelemetryManager.log("Create | Response: " + response.getResponseCode());
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            TelemetryManager.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> list(@RequestBody(required=false) Map<String, Object> map,
    		@RequestParam(value = "limit", required = false, defaultValue = "200") Integer limit,
    		@RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.list";
        Request request = getRequest(map);
        TelemetryManager.log("List all Items | TaxonomyId: " + taxonomyId + " | Request: " + request.getRequest());
        try {
        	ItemSetSearchCriteria criteria = new ItemSetSearchCriteria();
        	criteria.setResultSize(limit);
        	criteria.setStartPosition(offset);
        	request.put(AssessmentAPIParams.assessment_search_criteria.name(), criteria);
            Response response = assessmentManager.searchItemSets(taxonomyId, request);
            TelemetryManager.log("List Items | Response: " , response.getResult());
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            TelemetryManager.error("Create Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.update";
        Request request = getRequestObject(map);
        TelemetryManager.log("Update Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request);
        try {
            Response response = assessmentManager.updateItemSet(id, taxonomyId, request);
            TelemetryManager.log("Update | Response: " + response.getResponseCode());
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            TelemetryManager.error("Update | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "isfields", required = false) String[] isfields) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.read";
        TelemetryManager.log("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + isfields);
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
            @RequestParam(value = "isfields", required = false) String[] isfields) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemsset.generate";
        TelemetryManager.log("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + isfields);
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
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.search";
        TelemetryManager.log("Search | TaxonomyId: " + taxonomyId);
        try {
            Request reqeust = getSearchRequest(map);
            Response response = assessmentManager.searchItemSets(taxonomyId, reqeust);
            TelemetryManager.log("Search | Response: " + response.getResponseCode());
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Search | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.delete";
        TelemetryManager.log("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id);
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
