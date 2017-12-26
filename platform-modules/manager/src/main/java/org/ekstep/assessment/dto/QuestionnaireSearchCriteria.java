package org.ekstep.assessment.dto;

import org.ekstep.graph.dac.model.SearchCriteria;

import org.ekstep.common.dto.AbstractSearchCriteria;

public class QuestionnaireSearchCriteria extends AbstractSearchCriteria {

    @Override
    public SearchCriteria getSearchCriteria() {
        SearchCriteria sc = getSearchCriteria("Questionnaire");
        return sc;
    }

}
