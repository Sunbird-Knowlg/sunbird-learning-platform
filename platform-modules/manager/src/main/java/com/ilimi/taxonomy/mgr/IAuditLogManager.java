package com.ilimi.taxonomy.mgr;


import com.ilimi.common.dto.Response;
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;


public interface IAuditLogManager {

    void saveAuditRecord(AuditRecord audit);

    Response getAuditHistory(String graphId, String objectId);

    Response saveComment(String graphId, Comment comment);

    Response getComments(String graphId, String objectId);
    
    Response getCommentThread(String graphId, String objectId, String threadId);
}
