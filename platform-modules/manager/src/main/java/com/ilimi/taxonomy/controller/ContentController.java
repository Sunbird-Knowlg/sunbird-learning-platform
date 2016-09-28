package com.ilimi.taxonomy.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.dto.ContentDTO;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;

@Controller
@RequestMapping("/v1/content")
public class ContentController extends BaseController {

    private static LogHelper LOGGER = LogHelper.getInstance(ContentController.class.getName());

    @Autowired
    private IContentManager contentManager;

    private static final Map<String, String> objectTypeMap = new HashMap<String, String>();

    {
        objectTypeMap.put("game", "games");
        objectTypeMap.put("worksheet", "worksheets");
        objectTypeMap.put("screener", "screeners");
        objectTypeMap.put("story", "stories");
        objectTypeMap.put("asset", "assets");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "type", required = true) String objectType, @RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        objectType = objectType.trim().toLowerCase();
        String apiId = "content.create";
        if (objectTypeMap.containsKey(objectType)) {
            apiId = "content." + objectType + ".create";
            objectType = StringUtils.capitalize(objectType);
            Request request = getRequestObject(map, objectType);
            LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Request: " + request + " | user-id: " + userId);
            try {
                Response response = contentManager.create(taxonomyId, objectType, request);
                LOGGER.info("Create | Response: " + response);
                return getResponseEntity(response, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            } catch (Exception e) {
                LOGGER.error("Create | Exception: " + e.getMessage(), e);
                return getExceptionResponseEntity(e, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            }
        } else {
            return getExceptionResponseEntity(new ClientException("ERR_INVALID_CONTENT_TYPE", "ObjectType is invalid."),
                    apiId, null);
        }

    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "type", required = true) String objectType, @RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        objectType = objectType.toLowerCase();
        String apiId = "content.update";
        if (objectTypeMap.containsKey(objectType)) {
            apiId = "content." + objectType + ".update";
            objectType = StringUtils.capitalize(objectType);
            Request request = getRequestObject(map, objectType);
            LOGGER.info("Update | TaxonomyId: " + taxonomyId + " | Id: " + id + " | Request: " + request
                    + " | user-id: " + userId);
            try {
                Response response = contentManager.update(id, taxonomyId, objectType, request);
                LOGGER.info("Update | Response: " + response);
                return getResponseEntity(response, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            } catch (Exception e) {
                LOGGER.error("Update | Exception: " + e.getMessage(), e);
                return getExceptionResponseEntity(e, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            }
        } else {
            return getExceptionResponseEntity(new ClientException("ERR_INVALID_CONTENT_TYPE", "ObjectType is invalid."),
                    apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = false, defaultValue = "") String taxonomyId,
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "content.find";
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            Response findResp = contentManager.find(id, taxonomyId, fields);
            Response response = copyResponse(findResp);
            if (checkError(findResp)) {
                return getResponseEntity(findResp, apiId, null);
            }
            Node node = (Node) findResp.get(GraphDACParams.node.name());
            ContentDTO content = new ContentDTO(node);
            response.put("content", content.returnMap());
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
            @RequestParam(value = "type", required = true) String objectType,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestHeader(value = "user-id") String userId) {
        objectType = objectType.toLowerCase();
        String apiId = "content.findall";
        if (objectTypeMap.containsKey(objectType)) {
            apiId = "content." + objectType + ".findall";
            objectType = StringUtils.capitalize(objectType);
            LOGGER.info("FindAll | TaxonomyId: " + taxonomyId + " | fields: " + fields + " | user-id: " + userId);
            try {
                Response findAllResp = contentManager.findAll(taxonomyId, objectType, offset, limit, fields);
                Response response = copyResponse(findAllResp);
                if (checkError(response)) {
                    return getResponseEntity(response, apiId, null);
                }
                response.put(objectTypeMap.get(objectType.toLowerCase()),
                        findAllResp.get(ContentAPIParams.contents.name()));
                response.put(GraphDACParams.count.name(), findAllResp.get(GraphDACParams.count.name()));
                LOGGER.info("FindAll | Response: " + findAllResp);
                return getResponseEntity(response, apiId, null);
            } catch (Exception e) {
                LOGGER.error("FindAll | Exception: " + e.getMessage(), e);
                return getExceptionResponseEntity(e, apiId, null);
            }
        } else {
            return getExceptionResponseEntity(new ClientException("ERR_INVALID_CONTENT_TYPE", "ObjectType is invalid."),
                    apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "type", required = true) String objectType,
            @RequestHeader(value = "user-id") String userId) {
        objectType = objectType.toLowerCase();
        String apiId = "content.delete";
        if (objectTypeMap.containsKey(objectType)) {
            apiId = "content." + objectType + ".delete";
            objectType = StringUtils.capitalize(objectType);
            LOGGER.info("Delete | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
            try {
                Response response = contentManager.delete(id, taxonomyId);
                LOGGER.info("Delete | Response: " + response);
                return getResponseEntity(response, apiId, null);
            } catch (Exception e) {
                LOGGER.error("Delete | Exception: " + e.getMessage(), e);
                return getExceptionResponseEntity(e, apiId, null);
            }
        } else {
            return getExceptionResponseEntity(new ClientException("ERR_INVALID_CONTENT_TYPE", "ObjectType is invalid."),
                    apiId, null);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> list(
            @RequestParam(value = "taxonomyId", required = false, defaultValue = "") String taxonomyId,
            @RequestParam(value = "type", required = true) String objectType, @RequestBody Map<String, Object> map) {
        objectType = objectType.toLowerCase();
        String apiId = "content.list";
        if (objectTypeMap.containsKey(objectType)) {
            apiId = "content." + objectType + ".list";
            objectType = StringUtils.capitalize(objectType);
            Request request = getListRequestObject(map);
            LOGGER.info("List | Request: " + request);
            try {
                Response response = contentManager.listContents(taxonomyId, objectType, request);
                LOGGER.info("List | Response: " + response);
                return getResponseEntity(response, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            } catch (Exception e) {
                LOGGER.error("List | Exception: " + e.getMessage(), e);
                return getExceptionResponseEntity(e, apiId,
                        (null != request.getParams()) ? request.getParams().getMsgid() : null);
            }
        } else {
            return getExceptionResponseEntity(new ClientException("ERR_INVALID_CONTENT_TYPE", "ObjectType is invalid."),
                    apiId, null);
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestParam(value = "type", required = true) String objectType, @RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "content.search";
        LOGGER.info("Search | TaxonomyId: " + taxonomyId + " | user-id: " + userId);
        try {
            Request reqeust = getSearchRequest(map, objectType);
            Response response = contentManager.search(taxonomyId, objectType, reqeust);
            LOGGER.info("Search | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Search | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/bundle", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> bundle(@RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "content.archive";
        LOGGER.info("Create Content Bundle | user-id: " + userId);
        try {
            Request request = getBundleRequest(map, ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name());
            Response response = contentManager.bundle(request, null, "1.0");
            LOGGER.info("Archive | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Archive | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    private Request getSearchRequest(Map<String, Object> requestMap, String objectType) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        if (null != map && !map.isEmpty()) {
            try {
                ContentSearchCriteria criteria = mapper.convertValue(map, ContentSearchCriteria.class);
                criteria.setObjectType(objectType);
                request.put(ContentAPIParams.search_criteria.name(), criteria);
            } catch (Exception e) {
                throw new MiddlewareException(ContentErrorCodes.ERR_CONTENT_INVALID_SEARCH_CRITERIA.name(),
                        "Invalid search criteria.", e);
            }
        } else if (null != map && map.isEmpty()) {
            request.put(ContentAPIParams.search_criteria.name(), new ContentSearchCriteria());
        }
        return request;
    }

    @RequestMapping(value = "/upload/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> upload(@PathVariable(value = "id") String id,
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestHeader(value = "user-id") String userId,
            @RequestParam(value = "folder", required = false, defaultValue = "") String folder) {
        String apiId = "content.upload";
        LOGGER.info("Upload | Id: " + id + " | File: " + file + " | user-id: " + userId);
        try {
            String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.currentTimeMillis() + "."
                    + FilenameUtils.getExtension(file.getOriginalFilename());
            File uploadedFile = new File(name);
            file.transferTo(uploadedFile);
            Response response = contentManager.upload(id, taxonomyId, uploadedFile, folder);
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

    private Request getRequestObject(Map<String, Object> requestMap, String objectType) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        ObjectMapper mapper = new ObjectMapper();
        if (null != map && !map.isEmpty()) {
            try {
                Object obj = map.get(ContentAPIParams.content.name());
                if (null != obj) {
                    Node content = (Node) mapper.convertValue(obj, Node.class);
                    request.put(ContentAPIParams.content.name(), content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }
    
}
