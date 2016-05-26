package com.ilimi.dac.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AuditHistoryRecord implements Serializable {

	private static final long serialVersionUID = -5779950964487302125L;

	private Integer id;
    private String objectId;
    private String objectType;
    private String label;
    private String graphId;
    private String userId;
    private String requestId;
    private String logRecord;
    private String operation;
    private Date createdOn;
    
    public AuditHistoryRecord(){
    	
    }
    
	public AuditHistoryRecord(Integer id, String objectId, String objectType, String label, String graphId, String userId,
			String requestId, String logRecord, String operation, Date createdOn) {
		super();
		this.id = id;
		this.objectId = objectId;
		this.objectType = objectType;
		this.label = label;
		this.graphId = graphId;
		this.userId = userId;
		this.requestId = requestId;
		this.logRecord = logRecord;
		this.operation = operation;
		this.createdOn = createdOn;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getObjectId() {
		return objectId;
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
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
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
	public String getLogRecord() {
		return logRecord;
	}
	public void setLogRecord(String logRecord) {
		this.logRecord = logRecord;
	}
    public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

}
