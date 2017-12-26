package org.ekstep.orchestrator.dac.model;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class ScriptParams implements Serializable {

	private static final long serialVersionUID = 5935309052984980130L;
	private String name;
	private String datatype;
	private int index;
	@Field("routing_param")
	private boolean routingParam;
	@Field("routing_id")
	private String routingId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isRoutingParam() {
		return routingParam;
	}

	public void setRoutingParam(boolean routingParam) {
		this.routingParam = routingParam;
	}

	public String getRoutingId() {
		return routingId;
	}

	public void setRoutingId(String routingId) {
		this.routingId = routingId;
	}

}
