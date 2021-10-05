package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchConditions implements Serializable {

    private static final long serialVersionUID = 1218942087246380514L;
    public static final String LOGICAL_AND = "AND";
    public static final String LOGICAL_OR = "OR";

    public static final String OP_EQUAL = "=";
    public static final String OP_LIKE = "like";
    public static final String OP_STARTS_WITH = "startsWith";
    public static final String OP_ENDS_WITH = "endsWith";
    public static final String OP_GREATER_THAN = ">";
    public static final String OP_GREATER_OR_EQUAL = ">=";
    public static final String OP_LESS_THAN = "<";
    public static final String OP_LESS_OR_EQUAL = "<=";
    public static final String OP_NOT_EQUAL = "!=";
    public static final String OP_IN = "in";
    
    static List<String> operators = new ArrayList<String>();
    
    static {
        operators.add(OP_EQUAL);
        operators.add(OP_LIKE);
        operators.add(OP_STARTS_WITH);
        operators.add(OP_ENDS_WITH);
        operators.add(OP_GREATER_THAN);
        operators.add(OP_GREATER_OR_EQUAL);
        operators.add(OP_LESS_THAN);
        operators.add(OP_LESS_OR_EQUAL);
        operators.add(OP_NOT_EQUAL);
        operators.add(OP_IN);
    }
}
