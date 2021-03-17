package org.sunbird.dac.impl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.sunbird.dac.BaseDataAccessEntity;
import org.hibernate.annotations.Type;

/** The Class AuditHistoryEntity holds the column names used in mysql DB 
 * it extends the BaseDataAccessEntity which is base class for data access entites 
 */
@Entity
@Table(name = "AUDIT_HISTORY")
public class AuditHistoryEntity extends BaseDataAccessEntity {

	private static final long serialVersionUID = -4329702572102109449L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "AUDIT_HISTORY_ID_SEQ")
	@SequenceGenerator(name = "AUDIT_HISTORY_ID_SEQ", sequenceName = "AUDIT_HISTORY_ID_SEQ")
	@Column(name = "SMAH_PK_ID", length = 11)
	private Integer audit_id;

	@Column(name = "OBJECT_ID")
	private String objectId;

	@Column(name = "OBJECT_TYPE")
	private String objectType;

	@Column(name = "LOG_RECORD")
	@Type(type = "text")
	private String logRecord;

	@Column(name = "GRAPH_ID")
	private String graphId;

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "REQUEST_ID")
	private String requestId;

	@Column(name = "OPEARATION")
	private String operation;

	@Column(name = "LABEL")
	private String label;

	@Column(name = "SUMMARY")
	@Type(type = "text")
	private String summary;

	@Column(name = "CREATED_ON", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;

	@Override
	public Integer getId() {
		return this.audit_id;
	}

	@Override
	public void setId(Integer id) {
		this.audit_id = id;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getLogRecord() {
		return logRecord;
	}

	public void setLogRecord(String logRecord) {
		this.logRecord = logRecord;
	}

	public String getGraphId() {
		return graphId;
	}

	public void setGraphId(String graphId) {
		this.graphId = graphId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

}
