package com.ilimi.assessment.controller;

import java.util.ArrayList;
import java.util.List;
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
@RequestMapping("/v3/assessment/assessmentitems")
public class AssessmentItemV3Controller extends BaseController {

    private static LogHelper LOGGER = LogHelper.getInstance(AssessmentItemV3Controller.class.getName());

    @Autowired
    private IAssessmentManager assessmentManager;

    private static final String V2_GRAPH_ID = "domain";

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item.create";
        Request request = getRequestObject(map);
        LOGGER.info("Create Item | TaxonomyId: " + taxonomyId + " | Request: " + request);
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
    
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> list(@RequestBody(required=false) Map<String, Object> map,
    		@RequestParam(value = "limit", required = false, defaultValue = "200") Integer limit,
    		@RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item.list";
        Request request = getRequest(map);
        LOGGER.info("List all Items | TaxonomyId: " + taxonomyId + " | Request: " + request);
        try {
        	ItemSearchCriteria criteria = new ItemSearchCriteria();
        	criteria.setResultSize(limit);
        	criteria.setStartPosition(offset);
        	request.put(AssessmentAPIParams.assessment_search_criteria.name(), criteria);
            Response response = assessmentManager.searchAssessmentItems(taxonomyId, request);
            LOGGER.info("List Items | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Create Item | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item.update";
        Request request = getRequestObject(map);
        LOGGER.info("Update Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request);
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

    @RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "ifields", required = false) String[] ifields) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item.find";
        LOGGER.info("Find Item | TaxonomyId: " + taxonomyId + " | Id: " + id + " | ifields: " + ifields);
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
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item.search";
        LOGGER.info("Search | TaxonomyId: " + taxonomyId);
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

    @RequestMapping(value = "/retire/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id) {
    	String taxonomyId = V2_GRAPH_ID;
        String apiId = "assessment_item.delete";
        LOGGER.info("Delete Item | TaxonomyId: " + taxonomyId + " | Id: " + id);
        try {
            Response response = assessmentManager.deleteAssessmentItem(id, taxonomyId);
            LOGGER.info("Delete Item | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Delete Item | Exception: " + e.getMessage(), e);
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
