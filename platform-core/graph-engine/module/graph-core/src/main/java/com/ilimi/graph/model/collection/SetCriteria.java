package com.ilimi.graph.model.collection;

import java.io.Serializable;
import java.util.Map;

import com.ilimi.graph.model.ICriteria;

public class SetCriteria implements ICriteria, Serializable {

    private static final long serialVersionUID = -351057911022938583L;
    private String objectType;
    private Map<String, Object> criteria;

    public SetCriteria(String objectType) {
        this.objectType = objectType;
    }

    public SetCriteria(String objectType, Map<String, Object> criteria) {
        this.objectType = objectType;
        this.criteria = criteria;
    }

    @Override
    public Map<String, Object> getCriteria() {
        return this.criteria;
    }

    @Override
    public void setCriteria(Map<String, Object> criteria) {
        this.criteria = criteria;
    }

    @Override
    public String getObjectType() {
        return this.objectType;
    }

    @Override
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

}
