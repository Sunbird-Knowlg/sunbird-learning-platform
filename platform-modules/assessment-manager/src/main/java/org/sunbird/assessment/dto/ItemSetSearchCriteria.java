package org.sunbird.assessment.dto;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.AbstractSearchCriteria;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.SearchCriteria;

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
