package com.ilimi.graph.common.dto;

/**
 * Value Object for an identifier
 * 
 * @author rayulu
 * 
 */
public class BooleanValue extends BaseValueObject {

	/**
     * 
     */
    private static final long serialVersionUID = -3990119027189211410L;
    private Boolean value;
	
    public BooleanValue() {
    }
    
    public BooleanValue(Boolean id) {
        this.value = id;
    }	

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean id) {
		this.value = id;
	}

	@Override
	public String toString() {
		return "BooleanValue [" + (value != null ? "id=" + value : "") + "]";
	}

}
