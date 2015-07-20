package com.ilimi.assessment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ilimi.assessment.enums.AssessmentItemType;
import com.ilimi.assessment.enums.QuestionnaireType;
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
                    checkJsonList(metadata, errorMessages, "options", new String[] {"content_type", "content", "is_answer"}, AssessmentItemType.mcq.name());
                    break;
                case mmcq:
                    checkJsonList(metadata, errorMessages, "options", new String[] {"content_type", "content", "is_answer"}, AssessmentItemType.mmcq.name());
                    break;
                case ftb:
                    if(null == metadata.get("answer")) errorMessages.add("answer is missing.");
                    break;
                case mtf:
                    checkJsonList(metadata, errorMessages, "rhs_options", new String[] {"content_type", "content", "index"}, null);
                    checkJsonList(metadata, errorMessages, "lhs_options", new String[] {"content_type", "content", "index"}, null);
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
    public List<String> validateQuestionnaire(Node item) {
        List<String> errorMessages = new ArrayList<String>();
        Map<String, Object> metadata = item.getMetadata();
        String type = getQuestionnaireType(item);
        if(!QuestionnaireType.isValidQuestionnaireType(type)) {
            errorMessages.add("invalid questionnaire type" + type);
        } else {
            if(QuestionnaireType.materialised.name().equals(type)) {
                try {
                    List list = mapper.readValue(mapper.writeValueAsString(metadata.get("items")), List.class);
                    Integer total = (Integer) metadata.get("total_items");
                    if(list.size() < total) {
                        errorMessages.add("items has insufficient assessment items.");
                    }
                } catch (Exception e) {
                    errorMessages.add("invalid items array list.");
                }
            } else if(QuestionnaireType.dynamic.name().equals(type)) {
                List<String> dynamicErrors = new ArrayList<String>();
                checkJsonList(metadata, dynamicErrors, "item_sets", new String[] {"id", "count"}, null);
                if(dynamicErrors.size() == 0) {
                    try {
                        Integer total = (Integer) metadata.get("total_items");
                        List<Map<String, Object>> list = (List<Map<String, Object>>) mapper.readValue((String)metadata.get("items"), List.class);
                        Integer criteriaTotal = 0;
                        for(Map<String, Object>itemSet : list) {
                            Integer count = (Integer)itemSet.get("count");
                            criteriaTotal += count;
                        }
                        if(criteriaTotal < total) {
                            errorMessages.add("item sets has insufficient assessment items (count).");
                        }
                    } catch (Exception e) {
                    }
                } else {
                    errorMessages.addAll(dynamicErrors);
                }
            }
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
    private void checkJsonList(Map<String, Object> metadata, List<String> errorMessages, String propertyName, String[] keys, String itemType) {
        if(null == metadata.get(propertyName)) {
            errorMessages.add("item "+propertyName+" is missing.");
        } else {
            try {
                List<Map<String,Object>> values = mapper.readValue((String)metadata.get(propertyName), List.class);
                Integer answerCount = 0;
                for(Map<String, Object> value: values) {
                    for(String key : keys) {
                        if(!value.containsKey(key)) {
                            errorMessages.add("invalid assessment item property: "+propertyName+ ". "+key+" is missing.");
                            break;
                        }
                    }
                    if(itemType != null && (Boolean) value.get("is_answer")) answerCount++;
                        
                }
                if(AssessmentItemType.mcq.name().equals(itemType)) {
                    if(answerCount < 1) errorMessages.add("no option found with answer.");
                    else if(answerCount > 1) errorMessages.add("multiple answers found in a mcq assessment item.");
                } else if(AssessmentItemType.mmcq.name().equals(itemType)) {
                    if(answerCount <=1) errorMessages.add("there are no multiple answer options.");
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
