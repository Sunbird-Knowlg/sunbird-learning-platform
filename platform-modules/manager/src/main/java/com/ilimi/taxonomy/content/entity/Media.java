package com.ilimi.taxonomy.content.entity;

import java.util.List;

public class Media extends ECRFObject {
	
	private String src = null;
	private String type = null;
	private List<Plugin> childrenPlugin = null;
	
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<Plugin> getChildrenPlugin() {
		return childrenPlugin;
	}
	public void setChildrenPlugin(List<Plugin> childrenPlugin) {
		this.childrenPlugin = childrenPlugin;
	}
	
}
