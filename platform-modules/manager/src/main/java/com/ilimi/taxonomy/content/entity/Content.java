package com.ilimi.taxonomy.content.entity;

import java.util.List;

public class Content {
	
	private Manifest manifest = null;
	private List<Controller> controllers = null;
	private List<Plugin> plugins = null;
	
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
