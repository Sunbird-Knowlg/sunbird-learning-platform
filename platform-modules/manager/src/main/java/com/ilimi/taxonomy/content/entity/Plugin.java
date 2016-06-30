package com.ilimi.taxonomy.content.entity;

import java.util.List;

public class Plugin extends ECRFObject {
	
	private List<Plugin> childrenPlugin = null;
	private Manifest manifest = null;
	private List<Controller> controllers = null;
	private List<Event> events = null;
	
	public List<Plugin> getChildrenPlugin() {
		return childrenPlugin;
	}
	public void setChildrenPlugin(List<Plugin> childrenPlugin) {
		this.childrenPlugin = childrenPlugin;
	}
	public Manifest getManifest() {
		return manifest;
	}
	public void setManifest(Manifest manifest) {
		this.manifest = manifest;
	}
	public List<Controller> getControllers() {
		return controllers;
	}
	public void setControllers(List<Controller> controllers) {
		this.controllers = controllers;
	}
	public List<Event> getEvents() {
		return events;
	}
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
}
