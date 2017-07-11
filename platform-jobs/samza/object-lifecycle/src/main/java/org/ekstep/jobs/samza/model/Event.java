package org.ekstep.jobs.samza.model;

import java.util.HashMap;
import java.util.Map;

public class Event {

	private String eid;
	private long ets;
	private String mid;
	private String ver;
	private String channel = "";
	private Map<String, String> pdata = new HashMap<String, String>();
	private Map<String, Object> edata = new HashMap<String, Object>();

	public Event(String eid, String ver, String pid) {
		super();
		this.eid = eid;
		this.ver = ver;
		this.pdata.put("id", "org.ekstep.content.platform");
		this.pdata.put("pid", pid);
		this.pdata.put("ver", "1.0");
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

	public String getVer() {
		return ver;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Map<String, String> getPdata() {
		return pdata;
	}

	public void setPdata(Map<String, String> pdata) {
		this.pdata = pdata;
	}

	public Map<String, Object> getEdata() {
		return edata;
	}

	public void setEdata(Map<String, Object> edata) {
		this.edata = edata;
	}
	
	public void setEdata(LifecycleEvent event) {
		this.edata.put("eks", event.toMap());
	}

	@Override
	public String toString() {
		return "Event [eid=" + eid + ", ets=" + ets + ", ver=" + ver + ", channel=" + channel + ", pdata=" + pdata + ", edata=" + edata
				+ "]";
	}
	
	public Map<String, Object> getMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("eid", this.eid);
		map.put("ets", this.ets);
		map.put("mid", this.mid);
		map.put("ver", this.ver);
		map.put("channel", this.channel);
		map.put("pdata", this.pdata);
		map.put("edata", this.edata);
		return map;
	}
	
}
