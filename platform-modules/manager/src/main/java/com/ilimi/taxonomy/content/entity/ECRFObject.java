package com.ilimi.taxonomy.content.entity;

import java.util.Map;

public abstract class ECRFObject {
	
	private String id = null;
	private Map<String, Object> data = null;
	private String innerText = null;
	private String cData = null;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	public String getInnerText() {
		return innerText;
	}
	public void setInnerText(String innerText) {
		this.innerText = innerText;
	}
	public String getcData() {
		return cData;
	}
	public void setcData(String cData) {
		this.cData = cData;
	}
}
