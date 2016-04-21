package com.ilimi.taxonomy.content.entity;

import java.util.List;
import java.util.Map;

public class Event {
	
	private List<Action> actions = null;
	private Map<String, String> data = null;;

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}
	
}
