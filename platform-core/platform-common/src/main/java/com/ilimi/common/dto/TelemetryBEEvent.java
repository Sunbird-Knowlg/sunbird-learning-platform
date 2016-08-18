package com.ilimi.common.dto;

import java.util.HashMap;
import java.util.Map;

public class TelemetryBEEvent {

	private String eid;
	private long ets;
	private String ver;
	private Map<String, String> pdata;
	private Map<String, Object> edata;
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
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
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
	public void setPdata(String id, String pid, String ver, String uid) {
		this.pdata = new HashMap<String, String>();
		this.pdata.put("id", id);
		this.pdata.put("pid", pid);
		this.pdata.put("ver", ver);
		this.pdata.put("uid", uid);
	}
	public void setEdata(String cid, Object status, Object prevState, Object size, Object pkgVersion, Object concepts, Object flags) {
		this.edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		eks.put("cid", cid);
		eks.put("state", status);
		eks.put("prevstate", prevState);
		eks.put("size", size);
		eks.put("pkgVersion", pkgVersion);
		eks.put("concepts", concepts);
		eks.put("flags", concepts);
		
		edata.put("eks", eks);
	}
}
