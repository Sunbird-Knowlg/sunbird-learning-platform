package org.ekstep.assessment.handler;

import java.util.Map;

public interface IAssessmentHandler {

	String populateQuestion(String bodyMap);

    String populateAnswer(Map<String, Object> bodyMap);

}
