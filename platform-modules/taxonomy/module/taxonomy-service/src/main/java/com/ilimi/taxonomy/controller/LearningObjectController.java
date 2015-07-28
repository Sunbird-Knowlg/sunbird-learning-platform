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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.taxonomy.enums.LearningObjectAPIParams;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.IAuditLogManager;
import com.ilimi.taxonomy.mgr.ILearningObjectManager;

@Controller
@RequestMapping("/learning-object")
public class LearningObjectController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(LearningObjectController.class.getName());

    @Autowired
    private ILearningObjectManager lobManager;

    @Autowired
    IAuditLogManager auditLogManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAll(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "objectType", required = false) String objectType,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "gfields", required = false) String[] gfields, @RequestHeader(value = "user-id") String userId) {
        String apiId = "learning-object.list";
        LOGGER.info("FindAll | TaxonomyId: " + taxonomyId + " | Object Type: " + objectType + " | gfields: " + gfields + " | user-id: "
                + userId);
        try {
            Response response = lobManager.findAll(taxonomyId, objectType, offset, limit, gfields);
            LOGGER.info("FindAll | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("FindAll | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "gfields", required = false) String[] gfields, @RequestHeader(value = "user-id") String userId) {
        String apiId = "learning-object.find";
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = lobManager.find(id, taxonomyId, gfields);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "learning-object.create";
        Request request = getRequestObject(map);
        LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = lobManager.create(taxonomyId, request);
            LOGGER.info("Create | Response: " + response);
            AuditRecord audit = new AuditRecord(taxonomyId, null, "CREATE", response.getParams(), userId, map.get("request").toString(),
                    (String) map.get("COMMENT"));
            auditLogManager.saveAuditRecord(audit);
            return getResponseEntity(response, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/media", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> createMediaNode(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "media-object.create";
        Request request = getMediaRequestObject(map);
        
        LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = lobManager.createMedia(taxonomyId, request);
            LOGGER.info("Create | Response: " + response);
            AuditRecord audit = new AuditRecord(taxonomyId, null, "CREATE", response.getParams(), userId, map.get("request").toString(),
                    (String) map.get("COMMENT"));
            auditLogManager.saveAuditRecord(audit);
            return getResponseEntity(response, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "learning-object.update";
        Request request = getRequestObject(map);
        LOGGER.info("Update | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = lobManager.update(id, taxonomyId, request);
            LOGGER.info("Update | Response: " + response);
            AuditRecord audit = new AuditRecord(taxonomyId, id, "UPDATE", response.getParams(), userId, (String) map.get("request")
                    .toString(), (String) map.get("COMMENT"));
            auditLogManager.saveAuditRecord(audit);
            return getResponseEntity(response, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Update | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @SuppressWarnings("unchecked")
    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        ObjectMapper mapper = new ObjectMapper();
        if (null != map && !map.isEmpty()) {
            try {
            	Object objConcept = map.get(LearningObjectAPIParams.learning_object.name());
                if (null != objConcept) {
                    Node concept = (Node) mapper.convertValue(objConcept, Node.class);
                    request.put(LearningObjectAPIParams.learning_object.name(), concept);
                }
                Object objDefinitions = map.get(TaxonomyAPIParams.metadata_definitions.name());
                if (null != objDefinitions) {
                    String strObjDefinitions = mapper.writeValueAsString(objDefinitions);
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(strObjDefinitions.toString(),
                            List.class);
                    List<MetadataDefinition> definitions = new ArrayList<MetadataDefinition>();
                    for (Map<String, Object> metaMap : listMap) {
                        MetadataDefinition def = (MetadataDefinition) mapper.convertValue(metaMap, MetadataDefinition.class);
                        definitions.add(def);
                    }
                    request.put(TaxonomyAPIParams.metadata_definitions.name(), definitions);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }
    
    @SuppressWarnings("unchecked")
    private Request getMediaRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        System.out.println("Request Map:" + map);
        ObjectMapper mapper = new ObjectMapper();
        if (null != map && !map.isEmpty()) {
            try {
            	Object objConcept = map.get(LearningObjectAPIParams.media.name());
                if (null != objConcept) {
                    Node concept = (Node) mapper.convertValue(objConcept, Node.class);
                    request.put(LearningObjectAPIParams.media.name(), concept);
                }
                Object objDefinitions = map.get(TaxonomyAPIParams.metadata_definitions.name());
                if (null != objDefinitions) {
                    String strObjDefinitions = mapper.writeValueAsString(objDefinitions);
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(strObjDefinitions.toString(),
                            List.class);
                    List<MetadataDefinition> definitions = new ArrayList<MetadataDefinition>();
                    for (Map<String, Object> metaMap : listMap) {
                        MetadataDefinition def = (MetadataDefinition) mapper.convertValue(metaMap, MetadataDefinition.class);
                        definitions.add(def);
                    }
                    request.put(TaxonomyAPIParams.metadata_definitions.name(), definitions);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        String apiId = "learning-object.delete";
        LOGGER.info("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = lobManager.delete(id, taxonomyId);
            LOGGER.info("Delete | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Delete | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id1:.+}/{rel}/{id2:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> deleteRelation(@PathVariable(value = "id1") String fromLob,
            @PathVariable(value = "rel") String relationType, @PathVariable(value = "id2") String toLob,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        String apiId = "learning-object.delete.relation";
        LOGGER.info("Delete Relation | TaxonomyId: " + taxonomyId + " | StartId: " + fromLob + " | Relation: " + relationType
                + " | EndId: " + toLob);
        try {
            Response response = lobManager.deleteRelation(fromLob, relationType, toLob, taxonomyId);
            LOGGER.info("Delete Relation | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Delete Relation | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
