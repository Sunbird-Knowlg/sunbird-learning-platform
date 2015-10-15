package com.ilimi.taxonomy.dto;

import com.ilimi.common.dto.AbstractSearchCriteria;
import com.ilimi.graph.dac.model.SearchCriteria;

public class ContentSearchCriteria extends AbstractSearchCriteria {

    private String objectType;
    
    @Override
    public SearchCriteria getSearchCriteria() {
        SearchCriteria sc = getSearchCriteria(this.objectType);
        return sc;
    }
    
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
}
