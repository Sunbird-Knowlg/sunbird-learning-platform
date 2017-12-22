package org.ekstep.common.dto;

import java.util.HashMap;
import java.util.Map;

public class TelemetryBEAccessEvent {
	
	private String rid;
	private String uip;
	private String type;
	private String path;
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
		if(null == rid) {
			rid="";
		}
		return rid;
	}

	public void setRid(String rid) {
		if(null == rid) {
			rid="";
		}
		this.rid = rid;
	}

	public String getUip() {
		if(null == uip){
			uip="";
		}
		return uip;
	}

	public void setUip(String uip) {
		if(null == uip){
			uip="";
		}
		this.uip = uip;
	}

	public String getType() {
		if(null == type){
			type="";
		}
		return type;
	}

	public void setType(String type) {
		if(null == type){
			type="";
		}
		this.type = type;
	}

	public String getTitle() {
		if(null == title){
			title="";
		}
		return title;
	}

	public void setTitle(String title) {
		if(null == title){
			title="";
		}
		this.title = title;
	}

	public String getCategory() {
		if(null == category){
			category="";
		}
		return category;
	}

	public void setCategory(String category) {
		if(null == category){
			category="";
		}
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
		if(null == protocol){
			protocol="";
		}
		return protocol;
	}

	public void setProtocol(String protocol) {
		if(null == protocol){
			protocol="";
		}
		this.protocol = protocol;
	}

	public String getMethod() {
		if(null == method){
			method="";
		}
		return method;
	}

	public void setMethod(String method) {
		if(null == method){
			method="";
		}
		this.method = method;
	}

	public String getAction() {
		if(null == action){
			action="";
		}
		return action;
	}

	public void setAction(String action) {
		if(null == action){
			action="";
		}
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
		if(null == params){
			params=new HashMap<String,Object>();
		}
		return params;
	}

	public void setParams(Map<String, Object> parameterMap) {
		if(null == params){
			params=new HashMap<String,Object>();
		}
		this.params = parameterMap;
	}

	public String getPath() {
		if(null == path){
			path="";
		}
		return path;
	}

	public void setPath(String path) {
		if(null == path){
			path="";
		}
		this.path = path;
	}

}
