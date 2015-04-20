package com.ilimi.graph.common.dto;

/**
 * Value object for Idenifier which is String.
 * 
 * @author rayulu
 * 
 */
public class StringValue extends BaseValueObject {

    private static final long serialVersionUID = -4525229419937853210L;
    private String id;

    public StringValue() {
    }

    public StringValue(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "StringValue [" + (id != null ? "id=" + id : "") + "]";
    }

}
