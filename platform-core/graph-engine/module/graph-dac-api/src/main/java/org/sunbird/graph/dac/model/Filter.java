package org.sunbird.graph.dac.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.dac.enums.SystemProperties;

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
        if (StringUtils.equals("identifier", property)) {
            property = SystemProperties.IL_UNIQUE_ID.name();
        }
        if (SearchConditions.OP_EQUAL.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" = {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (SearchConditions.OP_LIKE.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" =~ {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, "(?i).*" + value + ".*");
            pIndex += 1;
        } else if (SearchConditions.OP_STARTS_WITH.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" =~ {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, "(?i)" + value + ".*");
            pIndex += 1;
        } else if (SearchConditions.OP_ENDS_WITH.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" =~ {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, "(?i).*" + value);
            pIndex += 1;
        } else if (SearchConditions.OP_GREATER_THAN.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" > {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (SearchConditions.OP_GREATER_OR_EQUAL.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" >= {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (SearchConditions.OP_LESS_THAN.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" < {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (SearchConditions.OP_LESS_OR_EQUAL.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" <= {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (SearchConditions.OP_NOT_EQUAL.equals(getOperator())) {
            sb.append(" NOT ").append(param).append(property).append(" = {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (SearchConditions.OP_IN.equals(getOperator())) {
        		sb.append(" ").append(param).append(property).append(" in {").append(pIndex).append("} ");
            sc.params.put("" + pIndex, value);
            pIndex += 1;
        } else if (SearchConditions.OP_IS.equals(getOperator())) {
            sb.append(" ").append(param).append(property).append(" is ").append(value).append(" ");
        }
        sc.pIndex = pIndex;
        return sb.toString();
    }
}
