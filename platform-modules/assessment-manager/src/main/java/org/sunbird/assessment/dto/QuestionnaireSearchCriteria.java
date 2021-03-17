package org.sunbird.assessment.dto;

import org.sunbird.common.dto.AbstractSearchCriteria;
import org.sunbird.graph.dac.model.SearchCriteria;

public class QuestionnaireSearchCriteria extends AbstractSearchCriteria {

    @Override
    public SearchCriteria getSearchCriteria() {
        SearchCriteria sc = getSearchCriteria("Questionnaire");
        return sc;
    }

}
