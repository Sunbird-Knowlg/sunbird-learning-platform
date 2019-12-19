package org.ekstep.assessment.handler;

import java.util.Map;

public interface IAssessmentHandler {

    Map<String, Object> populateQuestions(Map<String, Object> bodyMap);

    Map<String, Object> populateOptions(Map<String, Object> bodyMap);

    Map<String, Object> populateAnswers(Map<String, Object> bodyMap);

}
