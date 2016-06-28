package org.ekstep.searchindex.dto;

import java.util.List;
import java.util.Map;

public class SearchDTO {

	@SuppressWarnings("rawtypes")
	private List<Map> properties;
	private List<String> facets;
	private Map<String, String> sortBy;
	private String operation;
	private int limit;
	boolean traversalSearch = false;
	
	public SearchDTO() {
		super();
	}
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
	public List<String> getFacets() {
		return facets;
	}
	public void setFacets(List<String> facets) {
		this.facets = facets;
	}
	public Map<String, String> getSortBy() {
		return sortBy;
	}
	public void setSortBy(Map<String, String> sortBy) {
		this.sortBy = sortBy;
	}
	public boolean isTraversalSearch() {
		return traversalSearch;
	}
	public void setTraversalSearch(boolean traversalSearch) {
		this.traversalSearch = traversalSearch;
	}
}
