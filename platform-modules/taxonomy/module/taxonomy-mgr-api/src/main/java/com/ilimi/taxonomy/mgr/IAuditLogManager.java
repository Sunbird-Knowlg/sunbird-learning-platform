package com.ilimi.taxonomy.mgr;

import java.util.List;

import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.graph.common.Response;


public interface IAuditLogManager {

    void saveAuditRecord(AuditRecord audit);

    Response getAuditHistory(String graphId, String objectId);

    void saveComment(Comment comment);

    List<Comment> getComments(String graphId, String objectId);
}
