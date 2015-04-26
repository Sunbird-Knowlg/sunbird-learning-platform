package com.ilimi.graph.dac.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.common.dto.BaseValueObject;

/**
 * SearchCriteria provides a simple way for specifying criteria to search using
 * variable number of conditions. Multiple Criterion objects can be added to the
 * SearchCriteria object, with either AND or OR logic to be applied among the
 * several Criterion objects. SearchConditions class can be used to create
 * Criterion objects.
 * 
 * This also supports sorting on multiple fields, which can be specified using
 * setSortOrder(), sort(), asc() or desc() methods. Pagination is also
 * supported: maximum results per page can be specified using limit() method and
 * starting position of the result set can be specified using offset() method.
 * 
 * @author rayulu
 * 
 */
public class SearchCriteria extends BaseValueObject {

    private static final long serialVersionUID = -1686174755480352761L;

    private List<Criterion> criteriaEntries = new ArrayList<Criterion>();
    private List<Sort> sortOrder = new LinkedList<Sort>();
    private int resultSize = 0;
    private int startPosition = 0;
    private List<String> fields = new LinkedList<String>();
    private Map<String, Object> params = new HashMap<String, Object>();
    private int pIndex = 1;
    private boolean countQuery = false;

    public SearchCriteria() {
    }

    public SearchCriteria add(Criterion c) {
        this.criteriaEntries.add(c);
        return this;
    }

    public SearchCriteria sort(Sort sort) {
        if (null != sort)
            this.sortOrder.add(sort);
        return this;
    }

    public SearchCriteria asc(String fieldName) {
        if (StringUtils.isNotBlank(fieldName))
            this.sortOrder.add(new Sort(fieldName, Sort.SORT_ASC));
        return this;
    }

    public SearchCriteria desc(String fieldName) {
        if (StringUtils.isNotBlank(fieldName))
            this.sortOrder.add(new Sort(fieldName, Sort.SORT_DESC));
        return this;
    }

    public SearchCriteria limit(int limit) {
        if (limit > 0)
            this.resultSize = limit;
        return this;
    }

    public SearchCriteria offset(int offset) {
        if (offset > 0)
            this.startPosition = offset;
        return this;
    }

    public SearchCriteria returnField(String field) {
        if (StringUtils.isNotBlank(field))
            this.fields.add(field);
        return this;
    }

    public SearchCriteria returnFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public SearchCriteria countQuery(boolean count) {
        this.countQuery = count;
        return this;
    }

    public String getQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("MATCH (n:NODE) ");
        if (null != criteriaEntries && criteriaEntries.size() > 0) {
            sb.append("WHERE ");
            for (int i = 0; i < criteriaEntries.size(); i++) {
                sb.append(criteriaEntries.get(i).getCypher(this));
                if (i < criteriaEntries.size() - 1) {
                    sb.append("AND ");
                }
            }
        }
        if (!countQuery) {
            if (null == fields || fields.isEmpty()) {
                sb.append("RETURN n ");
            } else {
                sb.append("RETURN ");
                for (int i = 0; i < fields.size(); i++) {
                    sb.append("n.").append(fields.get(i)).append(" as ").append(fields.get(i)).append(" ");
                    if (i < fields.size() - 1)
                        sb.append(", ");
                }
            }
            if (null != sortOrder && sortOrder.size() > 0) {
                sb.append("ORDER BY ");
                for (Sort sort : sortOrder) {
                    sb.append("n.").append(sort.getSortField()).append(" ");
                    if (StringUtils.equals(Sort.SORT_DESC, sort.getSortOrder())) {
                        sb.append("DESC ");
                    }
                }
            }
            if (startPosition > 0) {
                sb.append("SKIP ").append(startPosition).append(" ");
            }
            if (resultSize > 0) {
                sb.append("LIMIT ").append(resultSize).append(" ");
            }
        } else {
            sb.append("RETURN count(n) ");
        }
        return sb.toString();
    }

    public Map<String, Object> getParams() {
        return this.params;
    }

    public List<String> getFields() {
        if (null == this.fields)
            this.fields = new ArrayList<String>();
        return this.fields;
    }

    int getParamIndex() {
        return this.pIndex;
    }

    void setParamIndex(int pIndex) {
        this.pIndex = pIndex;
    }
}
