package com.ilimi.taxonomy.controller;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Controller
@RequestMapping("/taxonomy")
public class TaxonomyController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(TaxonomyController.class.getName());

    @Autowired
    private ITaxonomyManager taxonomyManager;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAll() {
        LOGGER.info("FindAll");
        try {
            Response response = taxonomyManager.findAll();
            LOGGER.info("FindAll | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("FindAll | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "subgraph", defaultValue = "false") boolean subgraph) {
        LOGGER.info("Find | Id: " + id);
        try {
            Response response = taxonomyManager.find(id, subgraph);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@PathVariable(value = "id") String id, @RequestParam("file") MultipartFile file) {
        LOGGER.info("Create | Id: " + id + " | File: " + file);
        try {
            InputStream stream = null;
            if (null != file)
                stream = file.getInputStream();
            Response response = taxonomyManager.create(id, stream);
            LOGGER.info("Create | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id, @RequestParam("file") MultipartFile file) {
        LOGGER.info("Update | Id: " + id + " | File: " + file);
        try {
            InputStream stream = null;
            if (null != file)
                stream = file.getInputStream();
            Response response = taxonomyManager.update(id, stream);
            LOGGER.info("Update | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Update | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id) {
        LOGGER.info("Delete | Id: " + id);
        try {
            Response response = taxonomyManager.delete(id);
            LOGGER.info("Delete | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Delete | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}/concepts", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@PathVariable(value = "id") String id, @RequestBody Request request) {
        LOGGER.info("Search | Id: " + id + " | Request: " + request);
        try {
            Response response = taxonomyManager.search(id, request);
            LOGGER.info("Search | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Search | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}/definition", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> createDefinition(@PathVariable(value = "id") String id, @RequestBody Request request) {
        LOGGER.info("Create Definition | Id: " + id + " | Request: " + request);
        try {
            Response response = taxonomyManager.createDefinition(id, request);
            LOGGER.info("Create Definition | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Create Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}/definition/{defId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> updateDefinition(@PathVariable(value = "id") String id,
            @PathVariable(value = "defId") String objectType, @RequestBody Request request) {
        LOGGER.info("Update Definition | Id: " + id + " | Object Type: " + objectType + " | Request: " + request);
        try {
            Response response = taxonomyManager.updateDefinition(id, objectType, request);
            LOGGER.info("Update Definition | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Update Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}/definition/{defId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findDefinition(@PathVariable(value = "id") String id, @PathVariable(value = "defId") String objectType) {
        LOGGER.info("Find Definition | Id: " + id + " | Object Type: " + objectType);
        try {
            Response response = taxonomyManager.findDefinition(id, objectType);
            LOGGER.info("Find Definition | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Find Definition | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}/definition", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAllDefinitions(@PathVariable(value = "id") String id) {
        LOGGER.info("Find All Definitions | Id: " + id);
        try {
            Response response = taxonomyManager.findAllDefinitions(id);
            LOGGER.info("Find All Definitions | Response: " + response);
            return getResponseEntity(response);
        } catch (Exception e) {
            LOGGER.error("Find All Definitions | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e);
        }
    }

    @RequestMapping(value = "/{id}/definition/{defId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> deleteDefinition(@PathVariable(value = "id") String id, @PathVariable(value = "defId") String objectType) {
        LOGGER.info("Delete Definition | Id: " + id + " | Object Type: " + objectType);
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
