package org.ekstep.jobs.samza.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LifecycleEvent implements Serializable {

	private static final long serialVersionUID = -4119699570013488458L;

	private String type = "";
	private String id = "";
	private String subtype = "";
	private String parentid = "";
	private String parenttype = "";
	private String code = "";
	private String name = "";
	private String state = "";
	private String prevstate = "";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public String getParenttype() {
		return parenttype;
	}

	public void setParenttype(String parenttype) {
		this.parenttype = parenttype;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPrevstate() {
		return prevstate;
	}

	public void setPrevstate(String prevstate) {
		this.prevstate = prevstate;
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", this.id);
		map.put("parentid", this.parentid);
		map.put("parenttype", this.parenttype);
		map.put("type", this.type);
		map.put("subtype", this.subtype);
		map.put("code", this.code);
		map.put("name", this.name);
		map.put("state", this.state);
		map.put("prevstate", this.prevstate);
		return map;
	}

}
