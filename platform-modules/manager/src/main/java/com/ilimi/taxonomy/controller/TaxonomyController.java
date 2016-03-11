package com.ilimi.taxonomy.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.OutputStreamValue;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.IConceptManager;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Controller
@RequestMapping("/taxonomy")
public class TaxonomyController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(TaxonomyController.class.getName());

    @Autowired
    private ITaxonomyManager taxonomyManager;

    @Autowired
    private IConceptManager conceptManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAll(@RequestParam(value = "tfields", required = false) String[] tfields,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "taxonomy.list";
        LOGGER.info("FindAll | tfields: " + tfields + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.findAll(tfields);
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
            @RequestParam(value = "subgraph", defaultValue = "false") boolean subgraph,
            @RequestParam(value = "tfields", required = false) String[] tfields,
            @RequestParam(value = "cfields", required = false) String[] cfields,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "taxonomy.find";
        LOGGER.info("Find | Id: " + id + " | subgraph: " + subgraph + " | tfields: " + tfields + " | cfields: "
                + cfields + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.find(id, subgraph, tfields, cfields);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@PathVariable(value = "id") String id,
            @RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "taxonomy.import";
        LOGGER.info("Create | Id: " + id + " | File: " + file + " | user-id: " + userId);
        try {
            InputStream stream = null;
            if (null != file)
                stream = file.getInputStream();
            Response response = taxonomyManager.create(id, stream);
            LOGGER.info("Create | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "/{id:.+}/export", method = RequestMethod.POST)
    @ResponseBody
    public void export(@PathVariable(value = "id") String id,@RequestBody Map<String,Object> map,
            @RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String format = ImportType.CSV.name();
        LOGGER.info("Export | Id: " + id + " | Format: " + format + " | user-id: " + userId);
        try {
        	map.put(GraphEngineParams.format.name(), format);
            Response response = taxonomyManager.export(id, map);
            if (!checkError(response)) {
                OutputStreamValue graphOutputStream = (OutputStreamValue) response.get(GraphEngineParams.output_stream.name());
                OutputStream os = graphOutputStream.getOutputStream();
                ByteArrayOutputStream bos = (ByteArrayOutputStream) os;
                byte[] bytes = bos.toByteArray();
                resp.setContentType("text/csv");
                resp.setHeader("Content-Disposition", "attachment; filename=graph.csv");
                resp.getOutputStream().write(bytes);
                resp.getOutputStream().close();
            }
            LOGGER.info("Export | Response: " + response);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
            @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Delete | Id: " + id + " | user-id: " + userId);
        String apiId = "taxonomy.delete";
        try {
            Response response = taxonomyManager.delete(id);
            LOGGER.info("Delete | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Delete | Exception: " + e.getMessage(), e);
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}/concepts", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@PathVariable(value = "id") String id, @RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "concept.search";
        Request request = getRequestObject(map);
        LOGGER.info("Search | Id: " + id + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.search(id, request);
            LOGGER.info("Search | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Search | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        ObjectMapper mapper = new ObjectMapper();
        if (null != map && !map.isEmpty()) {
            try {
                Object objConcept = map.get(TaxonomyAPIParams.search_criteria.name());
                if (null != objConcept) {
                    SearchCriteria searchDTO = (SearchCriteria) mapper.convertValue(objConcept, SearchCriteria.class);
                    request.put(TaxonomyAPIParams.search_criteria.name(), searchDTO);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }

    @RequestMapping(value = "/{id:.+}/definition", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> createDefinition(@PathVariable(value = "id") String id, @RequestBody String json,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "definition.create";
        LOGGER.info("Create Definition | Id: " + id + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.updateDefinition(id, json);
            LOGGER.info("Create Definition | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Create Definition | Exception: " + e.getMessage(), e);
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}/definition/{defId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findDefinition(@PathVariable(value = "id") String id,
            @PathVariable(value = "defId") String objectType, @RequestHeader(value = "user-id") String userId) {
        String apiId = "definition.find";
        LOGGER.info("Find Definition | Id: " + id + " | Object Type: " + objectType + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.findDefinition(id, objectType);
            LOGGER.info("Find Definition | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}/definition", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAllDefinitions(@PathVariable(value = "id") String id,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "definition.list";
        LOGGER.info("Find All Definitions | Id: " + id + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.findAllDefinitions(id);
            LOGGER.info("Find All Definitions | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find All Definitions | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}/definition/{defId:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> deleteDefinition(@PathVariable(value = "id") String id,
            @PathVariable(value = "defId") String objectType, @RequestHeader(value = "user-id") String userId) {
        String apiId = "definition.delete";
        LOGGER.info("Delete Definition | Id: " + id + " | Object Type: " + objectType + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.deleteDefinition(id, objectType);
            LOGGER.info("Delete Definition | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Delete Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/hierarchy/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getTaxonomyHierarchy(@PathVariable(value = "id") String id,
            @RequestParam(value = "cfields", required = false) String[] cfields,
            @RequestHeader(value = "user-id") String userId) {
        if (cfields == null)
            cfields = new String[] { "name", "code" };
        String apiId = "taxonomy.hierarchy";
        LOGGER.info("Get Taxonomy Hierarchy | TaxonomyId: " + id + " | Id: " + id + " | Relation: " + "isParentOf"
                + " | Depth: " + 0 + " | user-id: " + userId);
        try {
            Response response = conceptManager.getConcepts(id, "isParentOf", 0, id, cfields, true);
            LOGGER.info("Get Taxonomy Hierarchy | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Get Taxonomy Hierarchy | Exception: " + e.getMessage(), e);
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{id:.+}/index", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> createIndex(@PathVariable(value = "id") String id,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "index.create";
        Request request = getRequest(map);
        LOGGER.info("Create Index | Id: " + id + " | Request: " + request + " | user-id: " + userId);
        try {
            List<String> keys = (List<String>) request.get(TaxonomyAPIParams.property_keys.name());
            Boolean unique = (Boolean) request.get(TaxonomyAPIParams.unique_constraint.name());
            Response response = taxonomyManager.createIndex(id, keys, unique);
            LOGGER.info("Create Index | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Create Index | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

}
