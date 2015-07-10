package com.ilimi.taxonomy.controller;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.taxonomy.mgr.IAuditLogManager;
import com.ilimi.util.AuditLogUtil;

@Controller
@RequestMapping("/comment")
public class CommentController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(CommentController.class.getName());

    @Autowired
    IAuditLogManager auditLogManager;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "comment.create";
        try {
            Comment comment = getCommentObject(taxonomyId, userId, map);
            LOGGER.info("Create | TaxonomyId: " + taxonomyId + " | Comment: " + comment + " | user-id: " + userId);
            Response response = auditLogManager.saveComment(taxonomyId, comment);
            LOGGER.info("Create | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Create | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @SuppressWarnings("unchecked")
    private Comment getCommentObject(String taxonomyId, String userId, Map<String, Object> requestMap) throws Exception {
        if (StringUtils.isBlank(taxonomyId)) {
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        }
        Comment comment = null;
        if (null != requestMap && !requestMap.isEmpty()) {
            Object requestObj = requestMap.get("request");
            if (null != requestObj) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> map = mapper.readValue(strRequest, Map.class);
                    Object objComment = map.get(CommonDACParams.comment.name());
                    if (null != objComment) {
                        comment = (Comment) mapper.convertValue(objComment, Comment.class);
                        String commentObjId = AuditLogUtil.createObjectId(taxonomyId, comment.getObjectId());
                        comment.setObjectId(commentObjId);
                        comment.setLastModifiedBy(userId);
                        comment.setLastModifiedOn(new Date());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return comment;
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> find(@PathVariable(value = "id") String id,
            @RequestParam(value = "taxonomyId", required = true) String taxonomyId, @RequestHeader(value = "user-id") String userId) {
        String apiId = "comment.list";
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            if (StringUtils.isBlank(taxonomyId)) {
                throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
            }
            Response response = auditLogManager.getComments(taxonomyId, id);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{id:.+}/{threadId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> findCommentThread(@PathVariable(value = "id") String id,
            @PathVariable(value = "threadId") String threadId, @RequestParam(value = "taxonomyId", required = true) String taxonomyId,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "comment.find";
        LOGGER.info("Find | TaxonomyId: " + taxonomyId + " | Id: " + id + " | user-id: " + userId);
        try {
            if (StringUtils.isBlank(taxonomyId)) {
                throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
            }
            Response response = auditLogManager.getCommentThread(taxonomyId, id, threadId);
            LOGGER.info("Find | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Find | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
