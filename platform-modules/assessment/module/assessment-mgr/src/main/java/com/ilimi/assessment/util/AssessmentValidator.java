package com.ilimi.assessment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ilimi.assessment.enums.AssessmentType;
import com.ilimi.graph.dac.model.Node;

@Component
public class AssessmentValidator {
    
    private ObjectMapper mapper = new ObjectMapper();

    public String getAssessmentType(Node item) {
        Map<String, Object> metadata = item.getMetadata();
        String itemType = (String) metadata.get("question_type");
        return itemType;
    }
    
    public List<String> validate(Node item) {
        List<String> errorMessages = new ArrayList<String>();
        String itemType = getAssessmentType(item);
        if(AssessmentType.isValidAssessmentType(itemType)) {
            Map<String, Object> metadata = item.getMetadata();
            checkJsonMap(metadata, errorMessages, "body", new String[] {"content_type", "content"});
            switch (AssessmentType.getAssessmentType(itemType)) {
                case mcq:
                case mmcq:
                    checkJsonList(metadata, errorMessages, "options", new String[] {"content_type", "content", "is_answer"});
                    break;
                case ftb:
                    if(null == metadata.get("answer")) errorMessages.add("answer is missing.");
                    break;
                case mtf:
                    checkJsonList(metadata, errorMessages, "rhs_options", new String[] {"content_type", "content", "index"});
                    checkJsonList(metadata, errorMessages, "lhs_options", new String[] {"content_type", "content", "index"});
                    break;
                case speech_question:
                    if(null == metadata.get("answer")) errorMessages.add("answer is missing.");
                    break;
                case canvas_question:
                    if(null == metadata.get("answer")) errorMessages.add("answer is missing.");
                    break;
                default:
                    errorMessages.add("invalid assessment type: "+itemType);
                    break;
            }
        } else {
            errorMessages.add("invalid assessment type: "+itemType);
        }
        return errorMessages;
    }
    
    @SuppressWarnings("unchecked")
    private void checkJsonMap(Map<String, Object> metadata, List<String> errorMessages, String propertyName, String[] keys) {
        if(null == metadata.get(propertyName)) {
            errorMessages.add("item "+propertyName+" is missing.");
        } else {
            try {
                Map<String,String> value = mapper.readValue((String)metadata.get(propertyName), Map.class);
                for(String key : keys) {
                    if(!value.containsKey(key)) {
                        errorMessages.add("invalid assessment item property: "+propertyName+ ". "+key+" is missing.");
                        break;
                    }
                }
            } catch (Exception e) {
                errorMessages.add("invalid assessment item property: "+propertyName+".");
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void checkJsonList(Map<String, Object> metadata, List<String> errorMessages, String propertyName, String[] keys) {
        if(null == metadata.get(propertyName)) {
            errorMessages.add("item "+propertyName+" is missing.");
        } else {
            try {
                List<Map<String,String>> values = mapper.readValue((String)metadata.get(propertyName), List.class);
                for(Map<String, String> value: values) {
                    for(String key : keys) {
                        if(!value.containsKey(key)) {
                            errorMessages.add("invalid assessment item property: "+propertyName+ ". "+key+" is missing.");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessages.add("invalid assessment item property: "+propertyName+".");
            }
        }
    }
}
