package com.ilimi.graph.common.dto;

import java.util.Map;

/**
 * 
 * @author Mahesh
 *
 */

public class BaseValueObjectMap<T> extends BaseValueObject {

	/**
     * 
     */
    private static final long serialVersionUID = 4624981258549365333L;
    private Map<String, T> baseValueMap = null;
	
	public BaseValueObjectMap(Map<String, T> baseValueMap) {
		super();
		this.baseValueMap = baseValueMap;
	}

	public BaseValueObjectMap() {
		super();
	}

	/**
	 * @return the baseValueMap
	 */
	public Map<String, T> getBaseValueMap() {
		return baseValueMap;
	}

	/**
	 * @param baseValueMap the baseValueMap to set
	 */
	public void setBaseValueMap(Map<String, T> baseValueMap) {
		this.baseValueMap = baseValueMap;
	}
	

}
