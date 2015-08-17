package com.ilimi.taxonomy.controller;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.dto.WorksheetDTO;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.mgr.IAuditLogManager;
import com.ilimi.taxonomy.mgr.IContentManager;

@Controller
@RequestMapping("/worksheet")
public class WorksheetController extends BaseController {
    
    private static Logger LOGGER = LogManager.getLogger(WorksheetController.class.getName());
    
    @Autowired
    private IContentManager contentManager;
    
    @Autowired
    IAuditLogManager auditLogManager;
    
    private static final String objectType = "Worksheet";
    
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "worksheet.create";
        Request request = getRequestObject(map);
        LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = contentManager.create(taxonomyId, objectType, request);
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
        String apiId = "worksheet.update";
        Request request = getRequestObject(map);
        LOGGER.info("Update | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request + " | user-id: " + userId);
        try {
            Response response = contentManager.update(id, taxonomyId, objectType, request);
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
    
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "fields", required = false) String[] fields, @RequestHeader(value = "user-id") String userId) {
        String apiId = "worksheet.find";
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response findResp = contentManager.find(id, taxonomyId, objectType, fields);
            Response response = copyResponse(findResp);
            if(checkError(findResp)) {
                return getResponseEntity(findResp, apiId, null);
            }
            Node node = (Node) findResp.get(GraphDACParams.node.name());
            WorksheetDTO worksheet = new WorksheetDTO(node);
            response.put(ContentAPIParams.worksheet.name(), worksheet);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findAll(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "fields", required = false) String[] fields, @RequestHeader(value = "user-id") String userId) {
        String apiId = "worksheet.findall";
        LOGGER.info("FindAll | TaxonomyId: " + taxonomyId + " | fields: " + fields
                + " | user-id: " + userId);
        try {
            Response findAllResp = contentManager.findAll(taxonomyId, objectType, offset, limit, fields);
            Response response = copyResponse(findAllResp);
            if(checkError(response)) {
                return getResponseEntity(response, apiId, null);
            }
            response.put(ContentAPIParams.worksheets.name(), findAllResp.get(ContentAPIParams.contents.name()));
            response.put(GraphDACParams.count.name(), findAllResp.get(GraphDACParams.count.name()));
            LOGGER.info("FindAll | Response: " + findAllResp);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("FindAll | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        String apiId = "worksheet.delete";
        LOGGER.info("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response response = contentManager.delete(id, taxonomyId);
            LOGGER.info("Delete | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Delete | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> list(@RequestBody Map<String, Object> map) {
        String apiId = "worksheet.list";
        Request request = getListRequestObject(map);
        LOGGER.info("List Worksheets | Request: " + request);
        try {
            Response response = contentManager.listContents(objectType, request);
            LOGGER.info("List Worksheets | Response: " + response);
            return getResponseEntity(response, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List Worksheets | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @RequestMapping(value = "/upload/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> upload(@PathVariable(value = "id") String id, @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        String apiId = "worksheet.upload";
        LOGGER.info("Upload | Id: " + id + " | File: " + file + " | user-id: " + userId);
        try {
            String name = FilenameUtils.getBaseName(file.getOriginalFilename())+"_"+System.currentTimeMillis()+"."+FilenameUtils.getExtension(file.getOriginalFilename());
            File uploadedFile = new File(name);
            file.transferTo(uploadedFile);
            
            Response response = contentManager.upload(id, taxonomyId, uploadedFile);
            LOGGER.info("Upload | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Upload | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @SuppressWarnings("unchecked")
    private Request getListRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        if (null != requestMap && !requestMap.isEmpty()) {
            Object requestObj = requestMap.get("request");
            if (null != requestObj) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> map = mapper.readValue(strRequest, Map.class);
                    request.setRequest(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return request;
    }
    
    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        ObjectMapper mapper = new ObjectMapper();
        if (null != map && !map.isEmpty()) {
            try {
                Object objWorksheet = map.get(ContentAPIParams.worksheet.name());
                if (null != objWorksheet) {
                    Node worksheet = (Node) mapper.convertValue(objWorksheet, Node.class);
                    request.put(ContentAPIParams.worksheet.name(), worksheet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }

}
