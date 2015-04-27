package com.ilimi.taxonomy.mgr.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.ilimi.taxonomy.dto.AuditRecordDTO;
import com.ilimi.taxonomy.dto.CommentDTO;
import com.ilimi.taxonomy.mgr.IAuditLogManager;

@Component
public class Log4jAuditLogManager implements IAuditLogManager {

    private static final Logger auditLogger = LogManager.getLogger("AuditTestLogger");
    
    @Async
    public void saveAuditRecord(AuditRecordDTO audit) {
        auditLogger.info(audit.getLogString());
    }

    @Override
    public List<AuditRecordDTO> getAuditHistory(String graphId, String objectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveComment(CommentDTO comment) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<CommentDTO> getComments(String graphId, String objectId) {
        // TODO Auto-generated method stub
        return null;
    }

}
