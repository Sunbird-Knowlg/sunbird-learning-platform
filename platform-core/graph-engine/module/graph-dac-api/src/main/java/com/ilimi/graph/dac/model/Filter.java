package com.ilimi.graph.dac.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class Filter implements Serializable {

    private static final long serialVersionUID = 6519813570430055306L;
    private String property;
    private Object value;
    private String operator;

    public Filter() {

    }

    public Filter(String property, Object value) {
        setProperty(property);
        setValue(value);
    }

    public Filter(String property, String operator, Object value) {
        setProperty(property);
        setOperator(operator);
        setValue(value);
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

    public String getOperator() {
        if (StringUtils.isBlank(this.operator) || !SearchConditions.operators.contains(this.operator))
            this.operator = SearchConditions.OP_EQUAL;
        return this.operator;
    }

    public void setOperator(String operator) {
        if (StringUtils.isBlank(operator) || !SearchConditions.operators.contains(operator))
            operator = SearchConditions.OP_EQUAL;
        this.operator = operator;
    }

    public String getCypher(SearchCriteria sc, String param) {
        StringBuilder sb = new StringBuilder();
        int pIndex = sc.pIndex;
        if (StringUtils.isBlank(param))
            param = "n";
        param = param + ".";
        if (getOperator() == SearchConditions.OP_EQUAL) {
            sb.append(" ").append(param).append(property).append(" = {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_LIKE) {
            sb.append(" ").append(param).append(property).append(" =~ {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, "*" + value + "*");
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_STARTS_WITH) {
            sb.append(" ").append(param).append(property).append(" =~ {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value + "*");
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_ENDS_WITH) {
            sb.append(" ").append(param).append(property).append(" =~ {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, "*" + value);
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_GREATER_THAN) {
            sb.append(" ").append(param).append(property).append(" > {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_GREATER_OR_EQUAL) {
            sb.append(" ").append(param).append(property).append(" >= {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_LESS_THAN) {
            sb.append(" ").append(param).append(property).append(" < {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_LESS_OR_EQUAL) {
            sb.append(" ").append(param).append(property).append(" <= {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_NOT_EQUAL) {
            sb.append(" ").append(param).append(property).append(" != {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (getOperator() == SearchConditions.OP_IN) {
            sb.append(" ANY (x in {").append(pIndex).append("} WHERE x in ").append(param).append(property).append(") ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        }
        sc.pIndex = pIndex;
        return sb.toString();
    }
}
