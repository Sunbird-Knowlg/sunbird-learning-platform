package com.ilimi.taxonomy.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.taxonomy.enums.LearningObjectAPIParams;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.ILearningObjectManager;

@Controller
@RequestMapping("/learning-object")
public class LearningObjectController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(LearningObjectController.class.getName());

    @Autowired
    private ILearningObjectManager lobManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAll(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "objectType", required = false) String objectType,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "gfields", required = false) String[] gfields) {
        LOGGER.info("FindAll | TaxonomyId: " + taxonomyId + " | Object Type: " + objectType + " | gfields: " + gfields);
        try {
            Response response = lobManager.findAll(taxonomyId, objectType, offset, limit, gfields);
            LOGGER.info("FindAll | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("FindAll | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "gfields", required = false) String[] gfields) {
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id);
        try {
            Response response = lobManager.find(id, taxonomyId, gfields);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestBody Map<String, Object> map) {
        Request request = getRequestObject(map);
        LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request);
        try {
            Response response = lobManager.create(taxonomyId, request);
            LOGGER.info("Create | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestBody Map<String, Object> map) {
        Request request = getRequestObject(map);
        LOGGER.info("Update | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request);
        try {
            Response response = lobManager.update(id, taxonomyId, request);
            LOGGER.info("Update | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Update | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = new Request();
        if (null != requestMap && !requestMap.isEmpty()) {
            Object requestObj = requestMap.get("request");
            if (null != requestObj) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> map = mapper.readValue(strRequest, Map.class);
                    Object objConcept = map.get(LearningObjectAPIParams.LEARNING_OBJECT.name());
                    if (null != objConcept) {
                        Node concept = (Node) mapper.convertValue(objConcept, Node.class);
                        request.put(LearningObjectAPIParams.LEARNING_OBJECT.name(), concept);
                    }
                    Object objDefinitions = map.get(TaxonomyAPIParams.METADATA_DEFINITIONS.name());
                    if (null != objDefinitions) {
                        String strObjDefinitions = mapper.writeValueAsString(objDefinitions);
                        List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(strObjDefinitions.toString(),
                                List.class);
                        List<MetadataDefinition> definitions = new ArrayList<MetadataDefinition>();
                        for (Map<String, Object> metaMap : listMap) {
                            MetadataDefinition def = (MetadataDefinition) mapper.convertValue(metaMap, MetadataDefinition.class);
                            definitions.add(def);
                        }
                        request.put(TaxonomyAPIParams.METADATA_DEFINITIONS.name(), new BaseValueObjectList<MetadataDefinition>(definitions));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return request;
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId) {
        LOGGER.info("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id);
        try {
            Response response = lobManager.delete(id, taxonomyId);
            LOGGER.info("Delete | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Delete | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id1:.+}/{rel}/{id2:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> deleteRelation(@PathVariable(value = "id1") String fromLob,
            @PathVariable(value = "rel") String relationType, @PathVariable(value = "id2") String toLob,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId) {
        LOGGER.info("Delete Relation | TaxonomyId: " + taxonomyId + " | StartId: " + fromLob + " | Relation: " + relationType
                + " | EndId: " + toLob);
        try {
            Response response = lobManager.deleteRelation(fromLob, relationType, toLob, taxonomyId);
            LOGGER.info("Delete Relation | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Delete Relation | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }
}
