package org.ekstep.assessment.handler;

import java.util.List;
import java.util.Map;

public interface IAssessmentHandler {

    Map<String, Object> populateQuestion(Map<String, Object> bodyMap);

    List<Map<String, Object>> populateOptions(Map<String, Object> bodyMap);

    Map<String, Object> populateAnswer(Map<String, Object> bodyMap);

}
