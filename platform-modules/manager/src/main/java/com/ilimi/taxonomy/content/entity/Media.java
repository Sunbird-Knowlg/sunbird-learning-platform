package com.ilimi.taxonomy.content.entity;

import java.util.List;
import java.util.Map;

public class Media {
	
	private Map<String, String> data = null;
	private List<Map<String, String>> childrenData = null;
	
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
	
}
