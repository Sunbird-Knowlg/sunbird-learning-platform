package com.ilimi.taxonomy.controller;

import java.io.InputStream;
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

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.dac.model.SearchDTO;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Controller
@RequestMapping("/taxonomy")
public class TaxonomyController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(TaxonomyController.class.getName());

    @Autowired
    private ITaxonomyManager taxonomyManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAll(@RequestParam(value = "tfields", required = false) String[] tfields,
            @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("FindAll | tfields: " + tfields + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.findAll(tfields);
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
            @RequestParam(value = "subgraph", defaultValue = "false") boolean subgraph,
            @RequestParam(value = "tfields", required = false) String[] tfields,
            @RequestParam(value = "cfields", required = false) String[] cfields, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Find | Id: " + id + " | subgraph: " + subgraph + " | tfields: " + tfields + " | cfields: " + cfields + " | user-id: "
                + userId);
        try {
            Response response = taxonomyManager.find(id, subgraph, tfields, cfields);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public void create(@PathVariable(value = "id") String id, @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
        LOGGER.info("Create | Id: " + id + " | File: " + file + " | user-id: " + userId);
        try {
            InputStream stream = null;
            if (null != file)
                stream = file.getInputStream();
            Response response = taxonomyManager.create(id, stream);
            LOGGER.info("Create | Response: " + response);
            String content = (String) response.get(TaxonomyAPIParams.taxonomy.name());
            writeToResponse(response.getParams(), content, "text/csv;charset=utf-8", resp);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            writeError(e, resp);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Delete | Id: " + id + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.delete(id);
            LOGGER.info("Delete | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Delete | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}/concepts", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@PathVariable(value = "id") String id, @RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        Request request = getRequestObject(map);
        LOGGER.info("Search | Id: " + id + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.search(id, request);
            LOGGER.info("Search | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Search | Exception: " + e.getMessage(), e);
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
                    Object objConcept = map.get(TaxonomyAPIParams.search_criteria.name());
                    if (null != objConcept) {
                        SearchDTO searchDTO = (SearchDTO) mapper.convertValue(objConcept, SearchDTO.class);
                        request.put(TaxonomyAPIParams.search_criteria.name(), searchDTO);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return request;
    }

    @RequestMapping(value = "/{id:.+}/definition", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> createDefinition(@PathVariable(value = "id") String id, @RequestBody String json,
            @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Create Definition | Id: " + id + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.updateDefinition(id, json);
            LOGGER.info("Create Definition | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Create Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}/definition/{defId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findDefinition(@PathVariable(value = "id") String id, @PathVariable(value = "defId") String objectType,
            @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Find Definition | Id: " + id + " | Object Type: " + objectType + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.findDefinition(id, objectType);
            LOGGER.info("Find Definition | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Find Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}/definition", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAllDefinitions(@PathVariable(value = "id") String id,
            @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Find All Definitions | Id: " + id + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.findAllDefinitions(id);
            LOGGER.info("Find All Definitions | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Find All Definitions | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id:.+}/definition/{defId:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> deleteDefinition(@PathVariable(value = "id") String id,
            @PathVariable(value = "defId") String objectType, @RequestHeader(value = "user-id") String userId) {
        LOGGER.info("Delete Definition | Id: " + id + " | Object Type: " + objectType + " | user-id: " + userId);
        try {
            Response response = taxonomyManager.deleteDefinition(id, objectType);
            LOGGER.info("Delete Definition | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Delete Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

}
