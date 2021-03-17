package org.sunbird.assessment.handler.impl;

import org.apache.commons.collections.MapUtils;
import org.sunbird.assessment.handler.IAssessmentHandler;

import java.util.HashMap;
import java.util.Map;

public class DefaultHandler implements IAssessmentHandler {
    private static IAssessmentHandler defaultHandler = null;


    private DefaultHandler() {
    }

    public static IAssessmentHandler getInstance() {
        if (defaultHandler == null)
            defaultHandler = new DefaultHandler();
        return defaultHandler;
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
