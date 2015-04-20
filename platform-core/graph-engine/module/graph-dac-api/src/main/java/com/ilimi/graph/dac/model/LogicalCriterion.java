package com.ilimi.graph.dac.model;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.common.dto.BaseValueObject;

class LogicalCriterion extends BaseValueObject implements Criterion {

    private static final long serialVersionUID = -7994028798804438207L;

    private Criterion lhs;
    private Criterion rhs;
    private String op;

    LogicalCriterion(Criterion lhs, Criterion rhs, String op) {
        this.lhs = lhs;
        this.rhs = rhs;
        setOp(op);
    }

    private void setOp(String op) {
        if (StringUtils.equalsIgnoreCase(SearchConditions.LOGICAL_OR, op))
            this.op = SearchConditions.LOGICAL_OR;
        else
            this.op = SearchConditions.LOGICAL_AND;
    }

    @Override
    public String getCypher(SearchCriteria sc) {
        StringBuilder sb = new StringBuilder();
        sb.append("( ");
        sb.append(lhs.getCypher(sc));
        if (StringUtils.equalsIgnoreCase(op, SearchConditions.LOGICAL_OR))
            sb.append(" OR ");
        else
            sb.append(" AND ");
        sb.append(rhs.getCypher(sc));
        sb.append(") ");
        return sb.toString();
    }

}
