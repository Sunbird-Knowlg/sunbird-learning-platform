package com.ilimi.assessment.controller;

import java.util.ArrayList;
import java.util.List;
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

import com.ilimi.assessment.dto.ItemSearchCriteria;
import com.ilimi.assessment.enums.AssessmentAPIParams;
import com.ilimi.assessment.enums.AssessmentErrorCodes;
import com.ilimi.assessment.mgr.IAssessmentManager;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.MetadataDefinition;

/**
 * 
 * @author mahesh
 *
 */

@Controller
@RequestMapping("/v1/assessmentitem")
public class AssessmentItemController extends BaseController {

    private static LogHelper LOGGER = LogHelper.getInstance(AssessmentItemController.class.getName());

    @Autowired
    private IAssessmentManager assessmentManager;

    private static final String V2_GRAPH_ID = "domain";

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(
            @RequestParam(value = "taxonomyId", required = false, defaultValue = V2_GRAPH_ID) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "assessment_item.create";
        Request request = getRequestObject(map);
        LOGGER.info("Create Item | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = assessmentManager.createAssessmentItem(taxonomyId, request);
            LOGGER.info("Create Item | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Create Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = false, defaultValue = V2_GRAPH_ID) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "assessment_item.update";
        Request request = getRequestObject(map);
        LOGGER.info("Update Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request
                + " | user-id: " + userId);
        try {
            Response response = assessmentManager.updateAssessmentItem(id, taxonomyId, request);
            LOGGER.info("Update Item | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Update Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = false, defaultValue = V2_GRAPH_ID) String taxonomyId,
            @RequestParam(value = "ifields", required = false) String[] ifields,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "assessment_item.find";
        LOGGER.info("Find Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + ifields + " | user-id: "
                + userId);
        try {
            Response response = assessmentManager.getAssessmentItem(id, taxonomyId, ifields);
            LOGGER.info("Find Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "/generate/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> generate(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = false, defaultValue = V2_GRAPH_ID) String taxonomyId,
            @RequestParam(value = "ifields", required = false) String[] ifields,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "assessment_item.find";
        LOGGER.info("Find Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + ifields + " | user-id: "
                + userId);
        try {
            Response response = assessmentManager.getAssessmentItem(id, taxonomyId, ifields);
            LOGGER.info("Find Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(
            @RequestParam(value = "taxonomyId", required = false, defaultValue = V2_GRAPH_ID) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "assessment_item.search";
        LOGGER.info("Search | TaxonomyId: " + taxonomyId + " | user-id: " + userId);
        try {
            Request reqeust = getSearchRequest(map);
            Response response = assessmentManager.searchAssessmentItems(taxonomyId, reqeust);
            LOGGER.info("Search | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Search | Exception: " + e.getMessage(), e);
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
            @RequestParam(value = "taxonomyId", required = false, defaultValue = V2_GRAPH_ID) String taxonomyId,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "assessment_item.delete";
        LOGGER.info("Delete Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = assessmentManager.deleteAssessmentItem(id, taxonomyId);
            LOGGER.info("Delete Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Delete Item | Exception: " + e.getMessage(), e);
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
