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
@Table(name = "VERSION")
public class VersionEntity extends BaseDataAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "VERSION_ID_SEQ")
    @SequenceGenerator(name = "VERSION_ID_SEQ", sequenceName = "VERSION_ID_SEQ")
    @Column(name = "SMV_PK_ID", length = 11)
    private Integer id;

    @Column(name = "OBJECT_ID")
    private String objectId;

    @Column(name = "VERSION_NUMBER")
    private String version;

    @Column(name = "PATH")
    private String path;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "COMMENT_ID")
    private CommentEntity comment;

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
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
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

}
