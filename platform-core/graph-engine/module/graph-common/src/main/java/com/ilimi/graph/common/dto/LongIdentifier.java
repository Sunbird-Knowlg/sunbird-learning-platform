package com.ilimi.graph.common.dto;

/**
 * @author rayulu
 * 
 */
public class LongIdentifier extends BaseValueObject {

    private static final long serialVersionUID = -2584851604384216276L;

    private Long id;

    public LongIdentifier() {
    }

    public LongIdentifier(Long id) {
        super();
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "LongIdentifier [" + (id != null ? "id=" + id : "") + "]";
    }

}
