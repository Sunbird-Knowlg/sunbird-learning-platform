package org.sunbird.content.entity;

import java.util.List;

public class Plugin extends ECRFObject {
	
	private List<Plugin> childrenPlugin = null;
	private Manifest manifest = null;
	private List<Controller> controllers = null;
	private List<Event> events = null;
	
	/**
     * @return the childrenPlugin
     */
	public List<Plugin> getChildrenPlugin() {
		return childrenPlugin;
	}
	
	/** 
     * @param ChildrenPlugin the ChildrenPlugin to set
     */
	public void setChildrenPlugin(List<Plugin> childrenPlugin) {
		this.childrenPlugin = childrenPlugin;
	}
	
	/**
     * @return the manifest
     */
	public Manifest getManifest() {
		return manifest;
	}
	
	/** 
     * @param manifest the Manifest to set
     */
	public void setManifest(Manifest manifest) {
		this.manifest = manifest;
	}
	
	/**
     * @return the controllers
     */
	public List<Controller> getControllers() {
		return controllers;
	}
	
	/** 
     * @param controllers the controllers to set
     */
	public void setControllers(List<Controller> controllers) {
		this.controllers = controllers;
	}
	
	/**
     * @return the events
     */
	public List<Event> getEvents() {
		return events;
	}
	
	/** 
     * @param events the events to set
     */
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
}
