package com.ilimi.graph.common.dto;

import java.util.Set;

/**
 * value object for list of BaseValueObject
 * 
 * @author rayulu
 *
 */
public class BaseValueObjectSet<T extends BaseValueObject> extends BaseValueObject {
	
	private static final long serialVersionUID = 6822932493694715438L;
	
	private Set<T> valueObjectSet = null;
	
	/**
	 * @return the valueObjectSet
	 */
	public Set<T> getValueObjectSet() {
		return valueObjectSet;
	}

	/**
	 * @param  the valueObjectSet to set
	 */
	public void setValueObjectSet(Set<T> valueObjectSet) {
		this.valueObjectSet = valueObjectSet;
	}
}
