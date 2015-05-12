package com.ilimi.dac.impl.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ilimi.dac.BaseDataAccessEntity;

@Entity
@Table(name = "AUDIT_LOG")
public class AuditLogEntity extends BaseDataAccessEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 2772878574889480638L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "AUDIT_LOG_ID_SEQ")
    @SequenceGenerator(name = "AUDIT_LOG_ID_SEQ", sequenceName = "AUDIT_LOG_ID_SEQ")
    @Column(name = "SMAL_PK_ID", length = 11)
    private Integer id;

    @Column(name = "OBJECT_ID")
    private String objectId;

    @Column(name = "OPERATION_TYPE")
    private String operationType;

    @Column(name = "LOG_RECORD")
    private String logRecord;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "COMMENT_ID")
    private CommentEntity comment;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "STATUS_MESSAGE")
    private String statusMessage;

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
     * @return the comment
     */
    public CommentEntity getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(CommentEntity comment) {
        this.comment = comment;
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

}
