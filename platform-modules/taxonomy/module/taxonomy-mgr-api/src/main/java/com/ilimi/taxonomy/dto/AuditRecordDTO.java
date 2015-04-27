package com.ilimi.taxonomy.dto;

import java.util.List;

import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.common.dto.Status;

public class AuditRecordDTO extends BaseValueObject {

    private static final long serialVersionUID = -7405314676577919799L;

    private String graphId;
    private List<String> objectIds;
    private String operation;
    private Status status;
    private String updatedBy;
    private String updatedOn;
    private String logRecord;
    private CommentDTO comment;
    
    

    public AuditRecordDTO() {
        super();
    }

    public AuditRecordDTO(String graphId, List<String> objectIds, String operation, Status status, String updatedBy, String logRecord,
            String comment) {
        super();
        this.graphId = graphId;
        this.objectIds = objectIds;
        this.operation = operation;
        this.status = status;
        this.updatedBy = updatedBy;
        this.updatedOn = ""+System.currentTimeMillis(); // TODO:
        this.logRecord = logRecord;
        this.comment = new CommentDTO(graphId, "", comment, updatedBy, updatedOn); // TODO: objectId.
        
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public List<String> getObjectIds() {
        return objectIds;
    }

    public void setObjectIds(List<String> objectIds) {
        this.objectIds = objectIds;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getLogRecord() {
        return logRecord;
    }

    public void setLogRecord(String logRecord) {
        this.logRecord = logRecord;
    }

    public CommentDTO getComment() {
        return comment;
    }

    public void setComment(CommentDTO comment) {
        this.comment = comment;
    }

    
    public String getLogString() {
        return "" + graphId + ", " + objectIds + ", " + operation + ", " + status
                + ", " + updatedBy + ", " + updatedOn + ", " + logRecord + ", " + comment.getComment() + "";
    }
    
    
}
