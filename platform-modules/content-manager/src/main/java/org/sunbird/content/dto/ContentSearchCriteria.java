package org.sunbird.content.dto;

import org.sunbird.common.dto.AbstractSearchCriteria;
import org.sunbird.graph.dac.model.SearchCriteria;

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
