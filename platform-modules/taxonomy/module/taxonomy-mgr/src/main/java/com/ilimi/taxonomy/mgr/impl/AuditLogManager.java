package com.ilimi.taxonomy.mgr.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.impl.AuditLogDataService;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.CommonsDacParams;
import com.ilimi.taxonomy.mgr.IAuditLogManager;
import com.ilimi.util.AuditLogUtil;

@Component
public class AuditLogManager implements IAuditLogManager {

    @Autowired
    AuditLogDataService auditLogDataService;
    
    @Override
    @Async
    public void saveAuditRecord(AuditRecord audit) {
        Request request = new Request();
        request.put(CommonsDacParams.AUDIT_RECORD.name(), audit);
        auditLogDataService.saveAuditLog(request);
    }

    @Override
    public Response getAuditHistory(String graphId, String objectId) {
        String auditObjectId = AuditLogUtil.createObjectId(graphId, objectId);
        Request request = new Request();
        request.put(CommonsDacParams.OBJECT_ID.name(), new StringValue(auditObjectId));
        Response response = auditLogDataService.getAuditHistory(request);
        return response;
    }

    @Override
    public void saveComment(Comment comment) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<Comment> getComments(String graphId, String objectId) {
        // TODO Auto-generated method stub
        return null;
    }

}
