package com.ilimi.graph.common.dto;

import java.util.List;

/**
 * value object for list of BaseValueObject
 * 
 */
public class BaseValueObjectList<T extends BaseValueObject> extends BaseValueObject {
	
	private static final long serialVersionUID = 6822932493694715438L;
	
	private List<T> valueObjectList = null;
	
	public BaseValueObjectList() {
		
	}
	
	public BaseValueObjectList(List<T> valueObjectList) {
		super();
		this.valueObjectList = valueObjectList;
	}
	
	/**
	 * @return the valueObjectList
	 */
	public List<T> getValueObjectList() {
		return valueObjectList;
	}

	/**
	 * @param operList the valueObjectList to set
	 */
	public void setValueObjectList(List<T> valueObjectList) {
		this.valueObjectList = valueObjectList;
	}
}
