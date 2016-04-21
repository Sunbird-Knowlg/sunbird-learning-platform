package com.ilimi.taxonomy.content.entity;

import java.util.List;
import java.util.Map;

public class Plugin {
	
	private Map<String, String> data = null;						// Always Represent attributes
	private List<Map<String, String>> childrenData = null;			// Always Represent children tags which is not plugin
	private List<Plugin> childrenPlugin = null;
	private List<Event> events = null;
	
	public Map<String, String> getData() {
		return data;
	}
	public void setData(Map<String, String> data) {
		this.data = data;
	}
	public List<Map<String, String>> getChildrenData() {
		return childrenData;
	}
	public void setChildrenData(List<Map<String, String>> childrenData) {
		this.childrenData = childrenData;
	}
	public List<Plugin> getChildrenPlugin() {
		return childrenPlugin;
	}
	public void setChildrenPlugin(List<Plugin> childrenPlugin) {
		this.childrenPlugin = childrenPlugin;
	}
	public List<Event> getEvents() {
		return events;
	}
	public void setEvents(List<Event> events) {
		this.events = events;
	}

}
