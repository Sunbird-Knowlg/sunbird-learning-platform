package org.sunbird.telemetry.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Telemetry V3 POJO to generate telemetry event.
 * 
 * @author mahesh
 *
 */

@JsonInclude(Include.NON_NULL)
public class Telemetry {

	private String eid;
	private long ets = System.currentTimeMillis();
	private String ver = "3.0";
	private String mid = "LP." + System.currentTimeMillis() + "." + UUID.randomUUID();;
	private Actor actor;
	private Context context;
	private Target object;
	private Map<String, Object> edata;
	private List<Map<String, Object>> cdata;
	private List<String> tags;
	private Long syncts = ets;

	/**
	 * @param eid
	 * @param ets
	 * @param ver
	 * @param actor
	 * @param context
	 * @param edata
	 */
	public Telemetry(String eid, long ets, String ver, Actor actor, Context context, Map<String, Object> edata) {
		super();
		this.eid = eid;
		this.ets = ets;
		this.ver = ver;
		this.actor = actor;
		this.context = context;
		this.edata = edata;
		this.syncts = ets;
	}

	/**
	 * @param eid
	 * @param actor
	 * @param context
	 * @param edata
	 */
	public Telemetry(String eid, Actor actor, Context context, Map<String, Object> edata) {
		super();
		this.eid = eid;
		this.actor = actor;
		this.context = context;
		this.edata = edata;
	}

	/**
	 * @param eid
	 * @param actor
	 * @param context
	 * @param edata
	 * @param cdata
	 */
	public Telemetry(String eid, Actor actor, Context context, Map<String, Object> edata,
			List<Map<String, Object>> cdata) {
		super();
		this.eid = eid;
		this.actor = actor;
		this.context = context;
		this.edata = edata;
		this.cdata = cdata;
	}

	/**
	 * @return the eid
	 */
	public String getEid() {
		return eid;
	}

	/**
	 * @param eid
	 *            the eid to set
	 */
	public void setEid(String eid) {
		this.eid = eid;
	}

	/**
	 * @return the ets
	 */
	public long getEts() {
		return ets;
	}

	/**
	 * @param ets
	 *            the ets to set
	 */
	public void setEts(long ets) {
		this.ets = ets;
	}

	/**
	 * @return the ver
	 */
	public String getVer() {
		return ver;
	}

	/**
	 * @param ver
	 *            the ver to set
	 */
	public void setVer(String ver) {
		this.ver = ver;
	}

	/**
	 * @return the mid
	 */
	public String getMid() {
		return mid;
	}

	/**
	 * @param mid
	 *            the mid to set
	 */
	public void setMid(String mid) {
		this.mid = mid;
	}

	/**
	 * @return the actor
	 */
	public Actor getActor() {
		return actor;
	}

	/**
	 * @param actor
	 *            the actor to set
	 */
	public void setActor(Actor actor) {
		this.actor = actor;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @return the object
	 */
	public Target getObject() {
		return object;
	}

	/**
	 * @param object
	 *            the object to set
	 */
	public void setObject(Target object) {
		this.object = object;
	}

	/**
	 * @return the edata
	 */
	public Map<String, Object> getEdata() {
		return edata;
	}

	/**
	 * @param edata
	 *            the edata to set
	 */
	public void setEdata(Map<String, Object> edata) {
		this.edata = edata;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return cdata
	 */
	public List<Map<String, Object>> getCdata() {
		return cdata;
	}

	/**
	 * @param cdata
	 */
	public void setCdata(List<Map<String, Object>> cdata) {
		this.cdata = cdata;
	}

	/**
	 * @return Long
	 */
	public Long getSyncts() {
		return syncts;
	}

	/**
	 * @param syncts
	 */
	public void setSyncts(Long syncts) {
		this.syncts = syncts;
	}
}