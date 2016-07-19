package com.ilimi.taxonomy.content.entity;

import java.util.List;

public class Event extends ECRFObject {
	
	private List<Plugin> childrenPlugin = null;

	public List<Plugin> getChildrenPlugin() {
		return childrenPlugin;
	}

	public void setChildrenPlugin(List<Plugin> childrenPlugin) {
		this.childrenPlugin = childrenPlugin;
	}
	
}
