package com.ilimi.common.dto;

import java.util.Map;

public class TelemetryBEAccessEvent {

	private String rid;
	private String uip;
	private String type;
	private String title;
	private String category;
	private int size;
	private long duration;
	private int status;
	private String protocol;
	private String method;
	private String action;
	private int value;
	private Map<String, String> context;
	private Map<String, Object> params;

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getUip() {
		return uip;
	}

	public void setUip(String uip) {
		this.uip = uip;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public Map<String, String> getContext() {
		return context;
	}

	public void setContext(Map<String, String> context) {
		this.context = context;
	}
	
	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> parameterMap) {
		this.params = parameterMap;
	}

}
