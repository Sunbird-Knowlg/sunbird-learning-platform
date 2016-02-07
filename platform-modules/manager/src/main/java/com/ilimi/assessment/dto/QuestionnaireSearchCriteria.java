package com.ilimi.assessment.dto;

import com.ilimi.common.dto.AbstractSearchCriteria;
import com.ilimi.graph.dac.model.SearchCriteria;

public class QuestionnaireSearchCriteria extends AbstractSearchCriteria {

    @Override
    public SearchCriteria getSearchCriteria() {
        SearchCriteria sc = getSearchCriteria("Questionnaire");
        return sc;
    }

}
