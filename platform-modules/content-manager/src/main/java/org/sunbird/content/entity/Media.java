package org.sunbird.content.entity;

import java.util.List;

public class Media extends ECRFObject {
	
	private String src = null;
	private String type = null;
	private List<Plugin> childrenPlugin = null;
	
	/**
     * @return the Src
     */
	public String getSrc() {
		return src;
	}
	
	/** 
     * @param src the Src to set
     */
	public void setSrc(String src) {
		this.src = src;
	}
	
	/**
     * @return the type
     */
	public String getType() {
		return type;
	}
	
	/** 
     * @param type the type to set
     */
	public void setType(String type) {
		this.type = type;
	}
	
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
	
}
