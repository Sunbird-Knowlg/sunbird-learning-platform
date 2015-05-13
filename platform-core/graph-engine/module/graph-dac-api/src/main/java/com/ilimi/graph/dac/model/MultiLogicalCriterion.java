package com.ilimi.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

class MultiLogicalCriterion implements Criterion, Serializable {

    private static final long serialVersionUID = -8008160073593271723L;

    private final List<Criterion> conditions = new ArrayList<Criterion>();
    private String op;

    MultiLogicalCriterion(String op) {
        setOp(op);
    }

    MultiLogicalCriterion(String op, Criterion... criterions) {
        setOp(op);
        if (null != criterions && criterions.length > 0) {
            List<Criterion> list = Arrays.asList(criterions);
            conditions.addAll(list);
        }
    }

    void add(Criterion c) {
        this.conditions.add(c);
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
        if (null != conditions && conditions.size() > 0) {
            sb.append("( ");
            for (int i = 0; i < conditions.size(); i++) {
                Criterion c = conditions.get(i);
                sb.append(c.getCypher(sc));
                if (i < (conditions.size() - 1)) {
                    if (StringUtils.equalsIgnoreCase(op, SearchConditions.LOGICAL_OR))
                        sb.append(" OR ");
                    else
                        sb.append(" AND ");
                }
            }
            sb.append(") ");
        }
        return sb.toString();
    }

}
