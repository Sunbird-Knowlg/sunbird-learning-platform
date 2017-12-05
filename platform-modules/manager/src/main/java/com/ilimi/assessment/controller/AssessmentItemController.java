package com.ilimi.assessment.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.MetadataDefinition;
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

import com.ilimi.assessment.dto.ItemSearchCriteria;
import com.ilimi.assessment.enums.AssessmentAPIParams;
import com.ilimi.assessment.enums.AssessmentErrorCodes;
import com.ilimi.assessment.mgr.IAssessmentManager;
import com.ilimi.common.controller.BaseController;

/**
 * 
 * @author mahesh
 *
 */

@Controller
@RequestMapping("/v1/assessmentitem")
public class AssessmentItemController extends BaseController {

    

    @Autowired
    private IAssessmentManager assessmentManager;

    private static final String V2_GRAPH_ID = "domain";

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.create";
        Request request = getRequestObject(map);
        PlatformLogger.log("Create Item | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = assessmentManager.createAssessmentItem(taxonomyId, request);
            PlatformLogger.log("Create Item | Response: " , response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            PlatformLogger.log("Create Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.update";
        Request request = getRequestObject(map);
        PlatformLogger.log("Update Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request
                + " | user-id: " + userId);
        try {
            Response response = assessmentManager.updateAssessmentItem(id, taxonomyId, request);
            PlatformLogger.log("Update Item | Response: " , response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            PlatformLogger.log("Update Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "ifields", required = false) String[] ifields,
            @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.info";
        PlatformLogger.log("Find Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + ifields + " | user-id: "
                + userId);
        try {
            Response response = assessmentManager.getAssessmentItem(id, taxonomyId, ifields);
            PlatformLogger.log("Find Item | Response: " , response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            PlatformLogger.log("Find Item | Exception: " , e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.search";
        PlatformLogger.log("Search | TaxonomyId: " + taxonomyId + " | user-id: " + userId);
        try {
            Request reqeust = getSearchRequest(map);
            Response response = assessmentManager.searchAssessmentItems(taxonomyId, reqeust);
            PlatformLogger.log("Search | Response: " , response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            PlatformLogger.log("Search | Exception: " , e.getMessage(), e);
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

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
            @RequestHeader(value = "user-id") String userId) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "ekstep.learning.item.delete";
        PlatformLogger.log("Delete Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = assessmentManager.deleteAssessmentItem(id, taxonomyId);
            PlatformLogger.log("Delete Item | Response: " , response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            PlatformLogger.log("Delete Item | Exception: " , e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
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
