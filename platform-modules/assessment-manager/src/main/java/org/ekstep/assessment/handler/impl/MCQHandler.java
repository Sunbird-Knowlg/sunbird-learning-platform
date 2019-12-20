package org.ekstep.assessment.handler.impl;

import org.apache.commons.collections.CollectionUtils;
import org.ekstep.assessment.handler.IAssessmentHandler;

import java.util.ArrayList;
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
    public Map<String, Object> populateQuestion(Map<String, Object> bodyMap) {
        return (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) bodyMap.getOrDefault("data", new HashMap<String, Object>())).getOrDefault("data", new HashMap<String, Object>())).getOrDefault("question", new HashMap<String, Object>());
    }

    @Override
    public List<Map<String,Object>> populateOptions(Map<String, Object> bodyMap) {
        return (List<Map<String, Object>>) ((Map<String, Object>) ((Map<String, Object>) bodyMap.getOrDefault("data", new HashMap<String, Object>())).getOrDefault("data", new HashMap<String, Object>())).getOrDefault("options", new ArrayList<Map<String, Object>>());
    }

    @Override
    public Map<String, Object> populateAnswer(Map<String, Object> bodyMap) {
        Map<String, Object> answersMap = new HashMap<>();
        bodyMap.entrySet().forEach(entry -> {
            List<Map<String, Object>> options = ((List<Map<String, Object>>) ((Map<String, Object>) ((Map<String, Object>) entry.getValue()).getOrDefault("data", new HashMap<String, Object>())).getOrDefault("options", new ArrayList<Map<String, Object>>()));
            if (CollectionUtils.isNotEmpty(options))
                answersMap.putAll(options.stream().filter(option -> (Boolean) option.getOrDefault("isCorrect", false)).findFirst().orElse(new HashMap<>()));
        });
        return answersMap;
    }

}
