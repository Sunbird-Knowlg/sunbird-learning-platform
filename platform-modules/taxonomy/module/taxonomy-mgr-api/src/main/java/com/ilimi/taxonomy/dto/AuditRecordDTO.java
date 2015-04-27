package com.ilimi.taxonomy.dto;

import java.util.List;

import com.ilimi.graph.common.dto.BaseValueObject;

public class AuditRecordDTO extends BaseValueObject {

    private static final long serialVersionUID = -7405314676577919799L;

    private String graphId;
    private List<String> objectIds;
    private String operation;
    private String updatedBy;
    private String updatedOn;
    private String logRecord;
    private CommentDTO comment;

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
}
