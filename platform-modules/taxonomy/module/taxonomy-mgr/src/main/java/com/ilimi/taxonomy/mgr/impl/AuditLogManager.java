package com.ilimi.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.impl.IAuditLogDataService;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.enums.CommonsDacParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.taxonomy.enums.AuditLogErrorCodes;
import com.ilimi.taxonomy.mgr.IAuditLogManager;
import com.ilimi.util.AuditLogUtil;

@Component
public class AuditLogManager implements IAuditLogManager {

    @Autowired
    IAuditLogDataService auditLogDataService;

    @Override
    @Async
    public void saveAuditRecord(AuditRecord audit) {
        if (null != audit) {
            if (StringUtils.isBlank(audit.getObjectId()) || StringUtils.isBlank(audit.getLogRecord())) {
                throw new ClientException(AuditLogErrorCodes.ERR_SAVE_AUDIT_MISSING_REQ_PARAMS.name(), "Required params missing...");
            }
            Request request = new Request();
            request.put(CommonsDacParams.audit_record.name(), audit);
            auditLogDataService.saveAuditLog(request);
        } else {
            throw new ClientException(AuditLogErrorCodes.ERR_INVALID_AUDIT_RECORD.name(), "audit record is null.");
        }

    }

    @Override
    public Response getAuditHistory(String graphId, String objectId) {
        String auditObjectId = AuditLogUtil.createObjectId(graphId, objectId);
        Request request = new Request();
        request.put(CommonsDacParams.object_id.name(), auditObjectId);
        Response response = auditLogDataService.getAuditHistory(request);
        return response;
    }

    @Override
    public Response saveComment(String graphId, Comment comment) {
        if (null != comment) {
            Request request = new Request();
            request.put(CommonsDacParams.comment.name(), comment);
            Response response = auditLogDataService.saveComment(request);
            return response;
        } else {
            throw new ClientException(AuditLogErrorCodes.ERR_INVALID_COMMENT.name(), "comment is null.");
        }

    }

    @Override
    public Response getComments(String graphId, String objectId) {
        String commentObjId = AuditLogUtil.createObjectId(graphId, objectId);
        Request request = new Request();
        request.put(CommonsDacParams.object_id.name(), commentObjId);
        Response response = auditLogDataService.getComments(request);
        return response;
    }

    public Response getCommentThread(String graphId, String objectId, String threadId) {
        String commentObjId = AuditLogUtil.createObjectId(graphId, objectId);
        Request request = new Request();
        request.put(CommonsDacParams.object_id.name(), commentObjId);
        request.put(CommonsDacParams.comment_thread_id.name(), threadId);
        Response response = auditLogDataService.getCommentThread(request);
        return response;
    }
}
