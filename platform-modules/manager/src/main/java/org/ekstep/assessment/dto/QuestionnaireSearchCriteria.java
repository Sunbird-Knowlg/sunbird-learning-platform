package org.ekstep.assessment.dto;

import org.ekstep.common.dto.AbstractSearchCriteria;
import org.ekstep.graph.dac.model.SearchCriteria;

public class QuestionnaireSearchCriteria extends AbstractSearchCriteria {

    @Override
    public SearchCriteria getSearchCriteria() {
        SearchCriteria sc = getSearchCriteria("Questionnaire");
        return sc;
    }

}
