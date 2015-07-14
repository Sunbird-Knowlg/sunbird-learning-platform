package com.ilimi.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MetadataCriterion implements Serializable {

    private static final long serialVersionUID = -6805135850224649022L;
    private List<Filter> filters;
    private String op;

    public String getOp() {
        if (StringUtils.isBlank(this.op))
            this.op = SearchConditions.LOGICAL_AND;
        return op;
    }

    public void setOp(String op) {
        if (StringUtils.equalsIgnoreCase(SearchConditions.LOGICAL_OR, op))
            this.op = SearchConditions.LOGICAL_OR;
        else
            this.op = SearchConditions.LOGICAL_AND;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }
    
    public void addFilter(Filter filter) {
        if (null == filters)
            filters = new ArrayList<Filter>();
        filters.add(filter);
    }

    public String getCypher(SearchCriteria sc, String param) {
        StringBuilder sb = new StringBuilder();
        if (null != filters && filters.size() > 0) {
            sb.append("( ");
            for (int i = 0; i < filters.size(); i++) {
                sb.append(filters.get(i).getCypher(sc, param));
                if (i < filters.size() - 1)
                    sb.append(" ").append(getOp()).append(" ");
            }
            sb.append(") ");
        }
        return sb.toString();
    }
}
