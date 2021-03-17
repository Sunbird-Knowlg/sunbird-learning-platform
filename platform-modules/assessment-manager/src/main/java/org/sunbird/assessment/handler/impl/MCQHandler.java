package org.sunbird.assessment.handler.impl;

import org.sunbird.assessment.handler.IAssessmentHandler;

import java.util.HashMap;
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
    public String populateAnswer(String answer) {
    		return answer;
    }

}
