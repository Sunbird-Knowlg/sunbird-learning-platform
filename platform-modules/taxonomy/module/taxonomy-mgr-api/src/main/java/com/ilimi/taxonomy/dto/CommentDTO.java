package com.ilimi.taxonomy.dto;

import com.ilimi.graph.common.dto.BaseValueObject;

public class CommentDTO extends BaseValueObject {

    private static final long serialVersionUID = -5413358718165866978L;

    private String graphId;
    private String objectId;
    private String comment;
    private String commentedBy;
    private String commentedOn;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentedBy() {
        return commentedBy;
    }

    public void setCommentedBy(String commentedBy) {
        this.commentedBy = commentedBy;
    }

    public String getCommentedOn() {
        return commentedOn;
    }

    public void setCommentedOn(String commentedOn) {
        this.commentedOn = commentedOn;
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
