package com.ilimi.graph.dac.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SearchDTO implements Serializable {

    private static final long serialVersionUID = -2675794987433037619L;
    private List<SearchDTO> criteria;
    private List<FilterDTO> filters;
    private String op;

    public List<SearchDTO> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<SearchDTO> criteria) {
        this.criteria = criteria;
    }

    public List<FilterDTO> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDTO> filters) {
        this.filters = filters;
    }

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

    public SearchCriteria searchCriteria() {
        SearchCriteria sc = new SearchCriteria();
        Criterion criterion = criteria(this);
        sc.add(criterion);
        return sc;
    }

    private Criterion criteria(SearchDTO dto) {
        MultiLogicalCriterion criterion = new MultiLogicalCriterion(dto.getOp());
        if (null != dto) {
            if (null != dto.getFilters() && !dto.getFilters().isEmpty()) {
                for (FilterDTO filter : dto.getFilters()) {
                    Criterion filterCriteria = new SimpleCriterion(filter.getProperty(), filter.getValue(), filter.getOperator());
                    criterion.add(filterCriteria);
                }
            }
            if (null != dto.getCriteria() && !dto.getCriteria().isEmpty()) {
                for (SearchDTO c : dto.getCriteria()) {
                    Criterion dtoCriteria = criteria(c);
                    criterion.add(dtoCriteria);
                }
            }
        }
        return criterion;
    }

}
