package com.ilimi.taxonomy.mgr.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;

import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.graph.common.Response;
import com.ilimi.taxonomy.mgr.IAuditLogManager;

public class Log4jAuditLogManager implements IAuditLogManager {

    private static final Logger auditLogger = LogManager.getLogger("AuditTestLogger");
    
    @Async
    public void saveAuditRecord(AuditRecord audit) {
        auditLogger.info(audit.toLogString());
    }

    @Override
    public Response getAuditHistory(String graphId, String objectId) {
        // TODO Auto-generated method stub
        return null;
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
