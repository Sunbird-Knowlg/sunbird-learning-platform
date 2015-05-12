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

import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.IAuditLogManager;
import com.ilimi.taxonomy.mgr.IConceptManager;

@Controller
@RequestMapping("/concept")
public class ConceptController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(ConceptController.class.getName());

    @Autowired
    private IConceptManager conceptManager;
    
    @Autowired
    IAuditLogManager auditLogManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAll(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "games", defaultValue = "false") boolean games,
            @RequestParam(value = "cfields", required = false) String[] cfields,
            @RequestParam(value = "gfields", required = false) String[] gfields, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("FindAll | TaxonomyId: " + taxonomyId + " | Games: " + games + " | cfields: " + cfields + " | gfields: " + gfields
                + " | user-id: " + userId);
        try {
            Response response = null;
            if (games) {
                response = conceptManager.getConceptsGames(taxonomyId, cfields, gfields);
            } else {
                response = conceptManager.findAll(taxonomyId, cfields);
            }
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
            @RequestParam(value = "cfields", required = false) String[] cfields, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | cfields: " + cfields + " | user-id: " + userId);
        try {
            Response response = conceptManager.find(id, taxonomyId, cfields);
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
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        Request request = getRequestObject(map);
        LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = conceptManager.create(taxonomyId, request);
            LOGGER.info("Create | Response: " + response);
            AuditRecord audit = new AuditRecord(taxonomyId, null, "CREATE", response.getParams(), userId, map.get("request").toString(), (String) map.get("COMMENT"));
            auditLogManager.saveAuditRecord(audit);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        Request request = getRequestObject(map);
        LOGGER.info("Update | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = conceptManager.update(id, taxonomyId, request);
            LOGGER.info("Update | Response: " + response);
            AuditRecord audit = new AuditRecord(taxonomyId, id, "UPDATE", response.getParams(), userId, map.get("request").toString(), (String) map.get("COMMENT"));
            auditLogManager.saveAuditRecord(audit);
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
                    Object objConcept = map.get(TaxonomyAPIParams.CONCEPT.name());
                    if (null != objConcept) {
                        Node concept = (Node) mapper.convertValue(objConcept, Node.class);
                        request.put(TaxonomyAPIParams.CONCEPT.name(), concept);
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
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = conceptManager.delete(id, taxonomyId);
            LOGGER.info("Delete | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Delete | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id1:.+}/{rel}/{id2:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> deleteRelation(@PathVariable(value = "id1") String fromConcept,
            @PathVariable(value = "rel") String relationType, @PathVariable(value = "id2") String toConcept,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Delete Relation | TaxonomyId: " + taxonomyId + " | StartId: " + fromConcept + " | Relation: " + relationType
                + " | EndId: " + toConcept + " | user-id: " + userId);
        try {
            Response response = conceptManager.deleteRelation(fromConcept, relationType, toConcept, taxonomyId);
            LOGGER.info("Delete Relation | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Delete Relation | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}/{rel}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getConcepts(@PathVariable(value = "id") String id, @PathVariable(value = "rel") String relationType,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "depth", required = false, defaultValue = "0") int depth, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Get Concepts | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Relation: " + relationType + " | Depth: " + depth
                + " | user-id: " + userId);
        try {
            Response response = conceptManager.getConcepts(id, relationType, depth, taxonomyId);
            LOGGER.info("Get Concepts | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Get Concepts | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }
    
}
