package com.ilimi.graph.dac.model;

import java.util.Map;

import com.ilimi.graph.common.dto.BaseValueObject;

class SimpleCriterion extends BaseValueObject implements Criterion {

    private static final long serialVersionUID = 7326586064722915660L;

    private String property;
    private Object value;
    private int operator;

    SimpleCriterion(String property, Object value, int operator) {
        this.property = property;
        this.value = value;
        setOperator(operator);
    }

    void setOperator(int operator) {
        if (operator > SearchConditions.OP_IN || operator < SearchConditions.OP_EQUAL)
            operator = SearchConditions.OP_EQUAL;
        this.operator = operator;
    }

    @Override
    public String getCypher(SearchCriteria sc) {
        
        StringBuilder sb = new StringBuilder();
        Map<String, Object> params = sc.getParams();
        int pIndex = sc.getParamIndex();
        if (operator == SearchConditions.OP_EQUAL) {
            sb.append(" n.").append(property).append(" = {").append(pIndex).append("} ");
            params.put("" + pIndex, value);
            pIndex += 1;
        } else if (operator == SearchConditions.OP_LIKE) {
            sb.append(" n.").append(property).append(" =~ '*").append(value).append("*' ");
        } else if (operator == SearchConditions.OP_STARTS_WITH) {
            sb.append(" n.").append(property).append(" =~ '").append(value).append("*' ");
        } else if (operator == SearchConditions.OP_ENDS_WITH) {
            sb.append(" n.").append(property).append(" =~ '*").append(value).append("' ");
        } else if (operator == SearchConditions.OP_GREATER_THAN) {
            sb.append(" n.").append(property).append(" > ").append(value).append(" ");
        } else if (operator == SearchConditions.OP_GREATER_OR_EQUAL) {
            sb.append(" n.").append(property).append(" >= ").append(value).append(" ");
        } else if (operator == SearchConditions.OP_LESS_THAN) {
            sb.append(" n.").append(property).append(" < ").append(value).append(" ");
        } else if (operator == SearchConditions.OP_LESS_OR_EQUAL) {
            sb.append(" n.").append(property).append(" <= ").append(value).append(" ");
        } else if (operator == SearchConditions.OP_NOT_EQUAL) {
            sb.append(" n.").append(property).append(" != {").append(pIndex).append("} ");
            params.put("" + pIndex, value);
            pIndex += 1;
        } else if (operator == SearchConditions.OP_IN) {
            sb.append(" n.").append(property).append(" in {").append(pIndex).append("} ");
            params.put("" + pIndex, value);
            pIndex += 1;
        }
        sc.setParamIndex(pIndex);
        return sb.toString();
    }

}
