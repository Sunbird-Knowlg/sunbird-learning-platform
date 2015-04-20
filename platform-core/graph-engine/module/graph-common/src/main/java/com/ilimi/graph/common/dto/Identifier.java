package com.ilimi.graph.common.dto;

/**
 * Value Object for an identifier
 * 
 * @author rayulu
 * 
 */
public class Identifier extends BaseValueObject {

	/**
     * 
     */
    private static final long serialVersionUID = 4965527036458473769L;
    private Integer id;
	
    public Identifier() {
    }
    
    public Identifier(Integer id) {
        this.id = id;
    }	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Identifier [" + (id != null ? "id=" + id : "") + "]";
	}

}
