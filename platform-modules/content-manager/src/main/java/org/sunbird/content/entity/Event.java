package org.sunbird.content.entity;

import java.util.List;

public class Event extends ECRFObject {
	
	private List<Plugin> childrenPlugin = null;

	/**
     * @return the childrenPlugin
     */
	public List<Plugin> getChildrenPlugin() {
		return childrenPlugin;
	}

	/** 
     * @param childrenPlugin the ChildrenPlugin to set
     */
	public void setChildrenPlugin(List<Plugin> childrenPlugin) {
		this.childrenPlugin = childrenPlugin;
	}
	
}
