package org.ekstep.assessment.handler.impl;

import org.ekstep.assessment.handler.IAssessmentHandler;

import java.util.Map;

public class MCQHandler implements IAssessmentHandler {

    private static IAssessmentHandler mcqHandler = null;


    private MCQHandler() {
    }

    public static IAssessmentHandler getInstance() {
        if (mcqHandler == null)
            mcqHandler = new MCQHandler();
        return mcqHandler;
    }

    @Override
    public String populateQuestion(String body) {
    		return body;
    }

    @Override
    public String populateAnswer(Map<String, Object> answerMap) {
        String answer = (String)((Map<String, Object>)((Map<String, Object>)answerMap.get("responseValue")).get("correct_response")).get("value");
        return answer;
    }

}
