package com.ilimi.dac.dto;

import java.util.Date;

import com.ilimi.graph.common.dto.BaseValueObject;

public class Comment extends BaseValueObject {

    private Integer id;
    private String title;
    private String comment;
    private String threadId;
    private String objectId;
    private String replyTo;
    private String lastModifiedBy;
    private Date lastModifiedOn;

    public Comment() {
        super();
    }

    public Comment(String objectId, String comment, String lastModifiedBy, Date lastModifiedOn) {
        super();
        this.objectId = objectId;
        this.comment = comment;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedOn = lastModifiedOn;
    }
    
    public Comment(String objectId, String threadId, String replyTo, String comment, String lastModifiedBy, Date lastModifiedOn) {
        super();
        this.objectId = objectId;
        this.threadId = threadId;
        this.replyTo = replyTo;
        this.comment = comment;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedOn = lastModifiedOn;
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
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the threadId
     */
    public String getThreadId() {
        return threadId;
    }

    /**
     * @param threadId
     *            the threadId to set
     */
    public void setThreadId(String threadId) {
        this.threadId = threadId;
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
     * @return the replyTo
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * @param replyTo
     *            the replyTo to set
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
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

}
