package org.ekstep.jobs.samza.model;

import java.util.HashMap;
import java.util.Map;

public class Event {

	private String eid;
	private long ets;
	private String mid;
	private Map<String, Object> actor;
	private Map<String, Object> context;
	private Map<String, Object> object;
	private Map<String, Object> edata;
	
	public Event(Map<String, Object> message){
		this.eid = (String)message.get("eid");
		this.ets = (long)message.get("ets");
		this.mid = (String)message.get("mid");
		this.actor = (Map<String, Object>)message.get("actor");
		this.context = (Map<String, Object>)message.get("context");
		this.object = (Map<String, Object>)message.get("object");
		this.edata = (Map<String, Object>)message.get("edata");
		
	}
	public String getEid() {
		return eid;
	}
	public void setEid(String eid) {
		this.eid = eid;
	}
	public long getEts() {
		return ets;
	}
	public void setEts(long ets) {
		this.ets = ets;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public Map<String, Object> getActor() {
		return actor;
	}
	public void setActor(Map<String, Object> actor) {
		this.actor = actor;
	}
	public Map<String, Object> getContext() {
		return context;
	}
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
	public Map<String, Object> getObject() {
		return object;
	}
	public void setObject(Map<String, Object> object) {
		this.object = object;
	}
	public Map<String, Object> getEdata() {
		return edata;
	}
	public void setEdata(Map<String, Object> edata) {
		this.edata = edata;
	}
	public Map<String, Object> getMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("eid", this.eid);
		map.put("ets", this.ets);
		map.put("mid", this.mid);
		map.put("actor", this.actor);
		map.put("context", this.context);
		map.put("object", this.object);
		map.put("edata", this.edata);
		map.put("@timestamp", System.currentTimeMillis());
		return map;
	}
}
