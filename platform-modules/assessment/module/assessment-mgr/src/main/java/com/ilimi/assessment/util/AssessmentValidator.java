package com.ilimi.assessment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ilimi.assessment.enums.AssessmentItemType;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

@Component
public class AssessmentValidator {
    
    private ObjectMapper mapper = new ObjectMapper();

    public String getAssessmentItemType(Node item) {
        Map<String, Object> metadata = item.getMetadata();
        String itemType = (String) metadata.get("question_type");
        return itemType;
    }
    
    public String getQuestionnaireType(Node item) {
        Map<String, Object> metadata = item.getMetadata();
        String itemType = (String) metadata.get("type");
        return itemType;
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getQuestionnaireItems(Node node) {
        Map<String, Object> metadata = node.getMetadata();
        List<String> memberIds = mapper.convertValue(metadata.get("items"), List.class);
        return memberIds;
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getQuestionnaireItemSets(Node node) {
        List<Map<String,String>> values = null;
        Map<String, Object> metadata = node.getMetadata();
        try {
            values = mapper.readValue((String)metadata.get("item_sets"), List.class);
        } catch (Exception e) {
        }
        return values;
    }
    
    public List<String> validateAssessmentItem(Node item) {
        List<String> errorMessages = new ArrayList<String>();
        String itemType = getAssessmentItemType(item);
        if(AssessmentItemType.isValidAssessmentType(itemType)) {
            Map<String, Object> metadata = item.getMetadata();
            checkJsonMap(metadata, errorMessages, "body", new String[] {"content_type", "content"});
            switch (AssessmentItemType.getAssessmentType(itemType)) {
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
    
    public List<String> validateQuestionnaire(Node item) {
        List<String> errorMessages = new ArrayList<String>();
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
                errorMessages.add("invalid assessment item property: "+propertyName+".");
            }
        }
    }

    public String getQuestionnaireSetId(Node node) {
        String setId = null;
        for(Relation relation : node.getOutRelations()) {
            if(SystemNodeTypes.SET.name().equals(relation.getEndNodeType())) {
                setId = relation.getEndNodeId();
                break;
            }
        }
        return setId;
    }

    public void compareMembers(List<String> inputMembers, List<String> existingMembers, List<String> addIds, List<String> removeIds) {
        if(null != inputMembers) {
            for(String member: inputMembers) {
                if(existingMembers.contains(member)) {
                   existingMembers.remove(member);
                } else {
                    addIds.add(member);
                }
            }
            removeIds.addAll(existingMembers);
        }
    }
}
