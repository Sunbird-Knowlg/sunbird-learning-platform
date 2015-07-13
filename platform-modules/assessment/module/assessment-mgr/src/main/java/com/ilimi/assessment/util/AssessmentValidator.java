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
        Map<String, Object> metadata = item.getMetadata();
        checkJsonMap(metadata, errorMessages, "body", new String[] {"content_type", "content"});
        if(AssessmentType.mcq.name().equals(itemType) || AssessmentType.mmcq.name().equals(itemType)) {
            checkJsonList(metadata, errorMessages, "options", new String[] {"content_type", "content", "is_answer"});
        } else if(AssessmentType.sort_list.name().equals(itemType)) {
            // TODO: check            
        } else if(AssessmentType.ftb.name().equals(itemType)) {
            // TODO: check            
        } else if(AssessmentType.mtf.name().equals(itemType)) {
            checkJsonList(metadata, errorMessages, "rhs_options", new String[] {"content_type", "content", "index"});
            checkJsonList(metadata, errorMessages, "lhs_options", new String[] {"content_type", "content", "index"});
        } else if(AssessmentType.speech_question.name().equals(itemType)) {
            // TODO: check
        } else if(AssessmentType.canvas_question.name().equals(itemType)) {
            // TODO: check            
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
