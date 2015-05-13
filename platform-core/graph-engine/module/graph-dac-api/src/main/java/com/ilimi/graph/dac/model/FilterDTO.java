package com.ilimi.graph.dac.model;

import java.io.Serializable;


public class FilterDTO implements Serializable {

    private static final long serialVersionUID = 1672074242526098695L;

    private int operator = SearchConditions.OP_EQUAL;
    private String property;
    private Object value;

    public FilterDTO() {
    }

    public FilterDTO(String property, Object value) {
        this.property = property;
        this.value = value;
    }

    public FilterDTO(String property, Object value, int operator) {
        this.property = property;
        this.value = value;
        this.setOperator(operator);
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        if (operator > SearchConditions.OP_IN || operator < SearchConditions.OP_EQUAL)
            operator = SearchConditions.OP_EQUAL;
        this.operator = operator;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
