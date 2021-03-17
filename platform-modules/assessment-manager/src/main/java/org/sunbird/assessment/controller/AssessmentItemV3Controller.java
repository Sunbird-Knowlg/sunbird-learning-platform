package org.sunbird.assessment.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sunbird.assessment.dto.ItemSearchCriteria;
import org.sunbird.assessment.enums.AssessmentAPIParams;
import org.sunbird.assessment.enums.AssessmentErrorCodes;
import org.sunbird.assessment.mgr.IAssessmentManager;
import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.sunbird.telemetry.logger.TelemetryManager;
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
@RequestMapping("/assessment/v3/items")
public class AssessmentItemV3Controller extends BaseController {

    

    @Autowired
    private IAssessmentManager assessmentManager;

    private static final String V2_GRAPH_ID = "domain";

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.create";
        Request request = getRequestObject(map);
        TelemetryManager.log("Create Item | TaxonomyId: " + taxonomyId + " | Request: " + request);
        try {
            Response response = assessmentManager.createAssessmentItem(taxonomyId, request);
            TelemetryManager.log("Create Item | Response: " , response.getResult());
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            TelemetryManager.error("Create Item | Exception: " + e.getMessage(), e);
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
        String apiId = "ekstep.learning.item.list";
        Request request = getRequest(map);
        TelemetryManager.log("List all Items | TaxonomyId: " + taxonomyId + " | Request: " + request);
        try {
        	ItemSearchCriteria criteria = new ItemSearchCriteria();
        	criteria.setResultSize(limit);
        	criteria.setStartPosition(offset);
        	request.put(AssessmentAPIParams.assessment_search_criteria.name(), criteria);
            Response response = assessmentManager.searchAssessmentItems(taxonomyId, request);
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
        String apiId = "ekstep.learning.item.update";
        Request request = getRequestObject(map);
        TelemetryManager.log("Update Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request);
        try {
            Response response = assessmentManager.updateAssessmentItem(id, taxonomyId, request);
            TelemetryManager.log("Update Item | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            TelemetryManager.error("Update Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
                                         @RequestParam(value = "ifields", required = false) String[] ifields,
                                         @RequestParam(value = "fields", required = false) String[] fields) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.read";
        TelemetryManager.log("Find Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + ifields);
        TelemetryManager.log("Find Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | fields: " + fields);

        try {
            Response response = assessmentManager.getAssessmentItem(id, taxonomyId, ifields, fields);
            TelemetryManager.log("Find Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Find Item | Exception: "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.search";
        TelemetryManager.log("Search | TaxonomyId: " + taxonomyId);
        try {
            Request reqeust = getSearchRequest(map);
            Response response = assessmentManager.searchAssessmentItems(taxonomyId, reqeust);
            TelemetryManager.log("Search | Response: " + response);
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
        String apiId = "ekstep.learning.item.delete";
        TelemetryManager.log("Delete Item | TaxonomyId: " + taxonomyId + " | Id: " + id);
        try {
            Response response = assessmentManager.deleteAssessmentItem(id, taxonomyId);
            TelemetryManager.log("Delete Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Delete Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    private Request getSearchRequest(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        if (null != map && !map.isEmpty()) {
            try {
                ItemSearchCriteria criteria = mapper.convertValue(map, ItemSearchCriteria.class);
                request.put(AssessmentAPIParams.assessment_search_criteria.name(), criteria);
            } catch (Exception e) {
                throw new MiddlewareException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_SEARCH_CRITERIA.name(),
                        "Invalid search criteria.", e);
            }
        } else if (null != map && map.isEmpty()) {
            request.put(AssessmentAPIParams.assessment_search_criteria.name(), new ItemSearchCriteria());
        }
        return request;
    }
    
    @SuppressWarnings("unchecked")
    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        if (null != map && !map.isEmpty()) {
            try {
                Object objConcept = map.get(AssessmentAPIParams.assessment_item.name());
                if (null != objConcept) {
                    Node item = (Node) mapper.convertValue(objConcept, Node.class);
                    request.put(AssessmentAPIParams.assessment_item.name(), item);
                }
                Object objDefinitions = map.get(AssessmentAPIParams.metadata_definitions.name());
                if (null != objDefinitions) {
                    String strObjDefinitions = mapper.writeValueAsString(objDefinitions);
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper
                            .readValue(strObjDefinitions.toString(), List.class);
                    List<MetadataDefinition> definitions = new ArrayList<MetadataDefinition>();
                    for (Map<String, Object> metaMap : listMap) {
                        MetadataDefinition def = (MetadataDefinition) mapper.convertValue(metaMap,
                                MetadataDefinition.class);
                        definitions.add(def);
                    }
                    request.put(AssessmentAPIParams.metadata_definitions.name(), definitions);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }
}
