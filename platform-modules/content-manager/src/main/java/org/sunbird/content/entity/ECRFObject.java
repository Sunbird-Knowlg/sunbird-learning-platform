package org.sunbird.content.entity;

import java.util.Map;

public abstract class ECRFObject {
	
	private String id = null;
	private Map<String, Object> data = null;
	private String innerText = null;
	private String cData = null;
	
	/**
     * @return the getId
     */
	public String getId() {
		return id;
	}
	 /**
     * @param setId the setId to set
     */
	public void setId(String id) {
		this.id = id;
	}
	/**
     * @return the data(Map<String, Object>)
     */
	public Map<String, Object> getData() {
		return data;
	}
	/** 
     * @param data(Map<String, Object>) the setData to set
     */
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	/**
     * @return the innerText
     */
	public String getInnerText() {
		return innerText;
	}
	/** 
     * @param innerText the innerText to set
     */
	public void setInnerText(String innerText) {
		this.innerText = innerText;
	}
	/**
     * @return the cData
     */
	public String getcData() {
		return cData;
	}
	/**
     * @param cData the cData to set
     */
	public void setcData(String cData) {
		this.cData = cData;
	}
}
