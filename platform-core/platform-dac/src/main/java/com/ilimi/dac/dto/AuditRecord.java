package com.ilimi.dac.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.common.ResponseParams;
import com.ilimi.util.AuditLogUtil;

public class AuditRecord implements Serializable {

    private static final long serialVersionUID = -5771216312853220439L;
    
    private Integer id;
    private String objectId;
    private String operationType;
    private String logRecord;
    private String status;
    private String statusCode;
    private String statusMessage;
    private Comment comment;
    private String lastModifiedBy;
    private Date lastModifiedOn;
    
    public AuditRecord() {
        super();
    }

    public AuditRecord(String graphId, String objectId, String operationType, ResponseParams params, String lastModifiedBy, String logRecord,
            String comment) {
        super();
        this.objectId = AuditLogUtil.createObjectId(graphId, objectId);
        this.operationType = operationType;
        this.status = params.toString();
        this.statusCode = params.getStatus();
        this.statusMessage = params.getErrmsg();
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedOn = new Date();
        this.logRecord = logRecord;
        if(null != comment) {
            this.comment = new Comment(objectId, comment, lastModifiedBy, this.lastModifiedOn);
        }
    }
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the objectId
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * @param objectId
     *            the objectId to set
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * @return the operationType
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * @param operationType
     *            the operationType to set
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * @return the logRecord
     */
    public String getLogRecord() {
        return logRecord;
    }

    /**
     * @param logRecord
     *            the logRecord to set
     */
    public void setLogRecord(String logRecord) {
        this.logRecord = logRecord;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the statusCode
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode
     *            the statusCode to set
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the statusMessage
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * @param statusMessage
     *            the statusMessage to set
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * @return the comment
     */
    public Comment getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(Comment comment) {
        this.comment = comment;
    }

    /**
     * @return the lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @param lastModifiedBy
     *            the lastModifiedBy to set
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * @return the lastModifiedOn
     */
    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    /**
     * @param lastModifiedOn
     *            the lastModifiedOn to set
     */
    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public String toLogString() {
        return StringUtils.chomp("" +  objectId + ", " + operationType + ", " + status
                + ", " + lastModifiedBy + ", " + lastModifiedOn + ", " + logRecord + ", " + comment.getComment() + "").replaceAll("\n", "::");
    }
}
