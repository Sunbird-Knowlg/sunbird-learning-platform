package com.ilimi.graph.common.dto;

/**
 * @author rayulu
 * 
 */
public class DoubleIdentifier extends BaseValueObject {

    private static final long serialVersionUID = -2584851604384216276L;

    private Double id;

    public DoubleIdentifier() {
    }

    public DoubleIdentifier(Double id) {
        super();
        this.id = id;
    }

    public Double getId() {
        return id;
    }

    public void setId(Double id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "DoubleIdentifier [" + (id != null ? "id=" + id : "") + "]";
    }

}
