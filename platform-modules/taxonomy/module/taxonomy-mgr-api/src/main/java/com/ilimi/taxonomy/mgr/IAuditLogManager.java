package com.ilimi.taxonomy.mgr;


import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;


public interface IAuditLogManager {

    void saveAuditRecord(AuditRecord audit);

    Response getAuditHistory(String graphId, String objectId);

    Response saveComment(String graphId, Comment comment);

    Response getComments(String graphId, String objectId);
}
