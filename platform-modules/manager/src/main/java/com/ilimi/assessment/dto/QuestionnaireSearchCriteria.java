package com.ilimi.assessment.dto;

import org.ekstep.graph.dac.model.SearchCriteria;

import com.ilimi.common.dto.AbstractSearchCriteria;

public class QuestionnaireSearchCriteria extends AbstractSearchCriteria {

    @Override
    public SearchCriteria getSearchCriteria() {
        SearchCriteria sc = getSearchCriteria("Questionnaire");
        return sc;
    }

}
