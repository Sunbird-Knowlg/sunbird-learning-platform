package org.ekstep.content.dto;

import org.ekstep.graph.dac.model.SearchCriteria;

import org.ekstep.common.dto.AbstractSearchCriteria;

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
