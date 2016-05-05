package com.ilimi.taxonomy.content.entity;

import java.util.List;
import java.util.Map;

public class Content {
	
	private Map<String, String> data = null; 
	private Manifest manifest = null;
	private List<Controller> controllers = null;
	private List<Plugin> plugins = null;
	
	public Map<String, String> getData() {
		return data;
	}
	public void setData(Map<String, String> data) {
		this.data = data;
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
	public List<Plugin> getPlugins() {
		return plugins;
	}
	public void setPlugins(List<Plugin> plugins) {
		this.plugins = plugins;
	}
}
