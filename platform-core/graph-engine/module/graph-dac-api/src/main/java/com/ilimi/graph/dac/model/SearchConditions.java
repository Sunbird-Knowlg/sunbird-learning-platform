package com.ilimi.graph.dac.model;

import java.util.List;

public class SearchConditions {

    static final String LOGICAL_AND = "AND";
    static final String LOGICAL_OR = "OR";

    static final int OP_EQUAL = 0;
    static final int OP_LIKE = 1;
    static final int OP_STARTS_WITH = 2;
    static final int OP_ENDS_WITH = 3;
    static final int OP_GREATER_THAN = 4;
    static final int OP_GREATER_OR_EQUAL = 5;
    static final int OP_LESS_THAN = 6;
    static final int OP_LESS_OR_EQUAL = 7;
    static final int OP_NOT_EQUAL = 8;
    static final int OP_IN = 9;

    public static Criterion and(Criterion lhs, Criterion rhs) {
        return new LogicalCriterion(lhs, rhs, LOGICAL_AND);
    }

    public static Criterion and(Criterion... criteria) {
        return new MultiLogicalCriterion(LOGICAL_AND, criteria);
    }

    public static Criterion or(Criterion lhs, Criterion rhs) {
        return new LogicalCriterion(lhs, rhs, LOGICAL_OR);
    }

    public static Criterion or(Criterion... criteria) {
        return new MultiLogicalCriterion(LOGICAL_OR, criteria);
    }

    public static Criterion eq(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_EQUAL);
    }

    public static Criterion ne(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_NOT_EQUAL);
    }

    public static Criterion like(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_LIKE);
    }

    public static Criterion startsWith(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_STARTS_WITH);
    }

    public static Criterion endsWith(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_ENDS_WITH);
    }

    public static Criterion gt(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_GREATER_THAN);
    }

    public static Criterion ge(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_GREATER_OR_EQUAL);
    }

    public static Criterion lt(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_LESS_THAN);
    }

    public static Criterion le(String propertyName, Object value) {
        return new SimpleCriterion(propertyName, value, OP_LESS_OR_EQUAL);
    }

    public static Criterion in(String propertyName, List<?> values) {
        return new SimpleCriterion(propertyName, values, OP_IN);
    }
}
