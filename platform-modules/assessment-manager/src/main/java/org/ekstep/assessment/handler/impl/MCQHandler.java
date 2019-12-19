package org.ekstep.assessment.handler.impl;

import org.ekstep.assessment.handler.IAssessmentHandler;

import java.util.HashMap;
import java.util.List;
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
    public Map<String, Object> populateQuestions(Map<String, Object> bodyMap) {
        return (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) bodyMap.get("data")).get("data")).get("question");
    }

    @Override
    public Map<String, Object> populateOptions(Map<String, Object> bodyMap) {
        return (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) bodyMap.get("data")).get("data")).get("options");
    }

    @Override
    public Map<String, Object> populateAnswers(Map<String, Object> bodyMap) {
        Map<String, Object> answersMap = new HashMap<>();
        bodyMap.entrySet().forEach(entry -> answersMap.put(entry.getKey(), ((List<Map<String, Object>>) ((Map<String, Object>) entry.getValue()).get("options"))
                .stream().filter(option -> (Boolean) option.get("isCorrect")).findFirst().get()));
        return answersMap;
    }
}
