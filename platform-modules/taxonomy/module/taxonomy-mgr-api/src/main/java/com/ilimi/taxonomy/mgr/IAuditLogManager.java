package com.ilimi.taxonomy.mgr;

import java.util.List;

import com.ilimi.taxonomy.dto.AuditRecordDTO;
import com.ilimi.taxonomy.dto.CommentDTO;

public interface IAuditLogManager {

    void saveAuditRecord(AuditRecordDTO audit);

    List<AuditRecordDTO> getAuditHistory(String graphId, String objectId);

    void saveComment(CommentDTO comment);

    List<CommentDTO> getComments(String graphId, String objectId);
}
