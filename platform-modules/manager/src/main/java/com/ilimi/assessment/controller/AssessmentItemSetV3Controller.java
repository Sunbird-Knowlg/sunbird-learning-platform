package com.ilimi.assessment.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

/**
 * 
 * @author mahesh
 *
 */

@Controller
@RequestMapping("/v3/assessment/itemsets")
public class AssessmentItemSetV3Controller extends BaseController {

    private static ILogger LOGGER = new PlatformLogger(AssessmentItemSetController.class.getName());

    private static final String V2_GRAPH_ID = "domain";

    @Autowired
    private IAssessmentManager assessmentManager;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.create";
        Request request = getRequestObject(map);
        LOGGER.log("Create | TaxonomyId: " + taxonomyId + " | Request: " + request);
        try {
            Response response = assessmentManager.createItemSet(taxonomyId, request);
            LOGGER.log("Create | Response: " , response.getResponseCode(), "INFO");
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.log("Create | Exception: " , e.getMessage(), e);
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
        LOGGER.log("List all Items | TaxonomyId: " + taxonomyId , " | Request: " + request, "INFO");
        try {
        	ItemSetSearchCriteria criteria = new ItemSetSearchCriteria();
        	criteria.setResultSize(limit);
        	criteria.setStartPosition(offset);
        	request.put(AssessmentAPIParams.assessment_search_criteria.name(), criteria);
            Response response = assessmentManager.searchItemSets(taxonomyId, request);
            LOGGER.log("List Items | Response: " , response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.log("Create Item | Exception: " , e.getMessage(), e);
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
        LOGGER.log("Update Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request);
        try {
            Response response = assessmentManager.updateItemSet(id, taxonomyId, request);
            LOGGER.log("Update | Response: " , response.getResponseCode(), "INFO");
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.log("Update | Exception: " , e.getMessage(), e);
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
        LOGGER.log("Find | TaxonomyId: " + taxonomyId , " | Id: " + id + " | ifields: " + isfields, "INFO");
        try {
            Response response = assessmentManager.getItemSet(id, taxonomyId, isfields, false);
            LOGGER.log("Find | Response: " , response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.log("Find | Exception: " , e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/generate/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> generate(@PathVariable(value = "id") String id,
            @RequestParam(value = "isfields", required = false) String[] isfields) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemsset.generate";
        LOGGER.log("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + isfields);
        try {
            Response response = assessmentManager.getItemSet(id, taxonomyId, isfields, true);
            LOGGER.log("Find | Response: " , response, "INFO");
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.log("Find | Exception: " , e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.search";
        LOGGER.log("Search | TaxonomyId: " + taxonomyId);
        try {
            Request reqeust = getSearchRequest(map);
            Response response = assessmentManager.searchItemSets(taxonomyId, reqeust);
            LOGGER.log("Search | Response: " , response.getResponseCode(), "INFO");
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.log("Search | Exception: " , e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.itemset.delete";
        LOGGER.log("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id);
        try {
            Response response = assessmentManager.deleteItemSet(id, taxonomyId);
            LOGGER.log("Delete | Response: " , response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.log("Delete | Exception: " , e.getMessage(), e);
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
