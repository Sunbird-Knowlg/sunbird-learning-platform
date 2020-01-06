package org.ekstep.assessment.handler.impl;

import org.apache.commons.collections.MapUtils;
import org.ekstep.assessment.handler.IAssessmentHandler;

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
    public String populateAnswer(Map<String, Object> answerMap) {
        Map<String, Object> responseValueMap = (Map<String, Object>) answerMap.getOrDefault("responseValue", new HashMap<>());
        if (MapUtils.isNotEmpty(responseValueMap))
            return (String) ((Map<String, Object>) responseValueMap.get("correct_response")).get("value");
        return "";
    }
}
