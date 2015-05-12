package com.ilimi.dac.impl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ilimi.dac.BaseDataAccessEntity;

@Entity
@Table(name = "SYS_MAS_COMMENT")
public class CommentEntity extends BaseDataAccessEntity {

    private static final long serialVersionUID = 3957407561703291843L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "COMMENT_ID_SEQ")
    @SequenceGenerator(name = "COMMENT_ID_SEQ", sequenceName = "COMMENT_ID_SEQ")
    @Column(name = "SMC_PK_ID", length = 11)
    private Integer id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "THREAD_ID")
    private String threadId;

    @Column(name = "OBJECT_ID")
    private String objectId;

    @Column(name = "REPLY_TO")
    private String replyTo;

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

}
