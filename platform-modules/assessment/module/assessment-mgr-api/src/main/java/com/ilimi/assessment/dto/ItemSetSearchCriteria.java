package com.ilimi.assessment.dto;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.AbstractSearchCriteria;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.SearchCriteria;

public class ItemSetSearchCriteria extends AbstractSearchCriteria {

    @Override
    public SearchCriteria getSearchCriteria() {
        String objectType = "ItemSet";
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.SET.name());
        if(StringUtils.isNotBlank(objectType)) sc.setObjectType(objectType);
        if(null != getMetadata()) sc.addMetadata(getMetadata());
        if(null != getRelations() && getRelations().size() > 0) {
            sc.setRelations(getRelations());
        }
        sc.setResultSize(getResultSize());
        sc.setStartPosition(getStartPosition());
        return sc;
    }

}
