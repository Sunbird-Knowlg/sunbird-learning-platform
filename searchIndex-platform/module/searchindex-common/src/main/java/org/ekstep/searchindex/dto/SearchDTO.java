package org.ekstep.searchindex.dto;

import java.util.List;
import java.util.Map;

public class SearchDTO {

	@SuppressWarnings("rawtypes")
	private List<Map> properties;
	private String operation;
	private int limit;
	
	@SuppressWarnings("rawtypes")
	public SearchDTO(List<Map> properties, String operation, int limit) {
		super();
		this.properties = properties;
		this.operation = operation;
		this.limit = limit;
	}
	@SuppressWarnings("rawtypes")
	public List<Map> getProperties() {
		return properties;
	}
	@SuppressWarnings("rawtypes")
	public void setProperties(List<Map> properties) {
		this.properties = properties;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	
	
}
