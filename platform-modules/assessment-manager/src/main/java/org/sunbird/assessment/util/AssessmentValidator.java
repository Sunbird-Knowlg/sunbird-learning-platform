package org.sunbird.assessment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.assessment.enums.AssessmentItemType;
import org.sunbird.assessment.enums.QuestionnaireType;
import org.sunbird.common.mgr.BaseManager;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

@Component
public class AssessmentValidator extends BaseManager {

    private ObjectMapper mapper = new ObjectMapper();

    public String getAssessmentItemType(Node item) {
        Map<String, Object> metadata = item.getMetadata();
        String itemType = (String) metadata.get("type");
        return itemType;
    }

    public String getQuestionnaireType(Node item) {
        Map<String, Object> metadata = item.getMetadata();
        String itemType = (String) metadata.get("type");
        return itemType;
    }

    public void checkAnswers(Map<String, Object> metadata, List<String> errorMessages, Integer numAnswers) {
        if (null != numAnswers) {
            try {
                @SuppressWarnings("rawtypes")
                Map map = mapper.readValue(metadata.get("answer").toString(), Map.class);
                if (numAnswers != map.size())
                    errorMessages.add("num_answers is not equals to no. of correct answer.");
            } catch (Exception e) {
                errorMessages.add("answer value is invalid");
            }
        }
    }
    
    public List<String> validateAssessmentItem(Node item) {
        List<String> errorMessages = new ArrayList<String>();
        String itemType = getAssessmentItemType(item);
        Map<String, Object> metadata = item.getMetadata();
        Integer numAnswers = null;
        if (isNotBlank(metadata, "num_answers"))
            numAnswers = (int) metadata.get("num_answers");
        if (isNotBlank(metadata, "hints"))
            checkJsonList(metadata, errorMessages, "hints",
                    new String[] { "order", "anchor", "content_type", "content", "start", "timing", "on_next" },
                    null);
        if (isNotBlank(metadata, "responses"))
        	validateResponses(metadata.get("responses"), errorMessages);
        if(StringUtils.isNotEmpty(itemType)) {
            switch (AssessmentItemType.getAssessmentType(itemType)) {
                case mcq:
                    checkJsonList(metadata, errorMessages, "options",
                            new String[]{"value"}, AssessmentItemType.mcq.name());
                    break;
                case mmcq:
                    checkJsonList(metadata, errorMessages, "options",
                            new String[]{"value"}, AssessmentItemType.mmcq.name());
                    break;
                case ftb:
                    if (null == metadata.get("answer"))
                        errorMessages.add("answer is missing.");
                    checkAnswers(metadata, errorMessages, numAnswers);
                    break;
                case mtf:
                    checkMtfJson(metadata, errorMessages);
                    break;
                case speech_question:
                    if (null == metadata.get("answer"))
                        errorMessages.add("answer is missing.");
                    checkAnswers(metadata, errorMessages, numAnswers);
                    break;
                case canvas_question:
                    if (null == metadata.get("answer"))
                        errorMessages.add("answer is missing.");
                    checkAnswers(metadata, errorMessages, numAnswers);
                    break;
                case recognition:
                    checkJsonList(metadata, errorMessages, "options",
                            new String[]{"value"}, AssessmentItemType.recognition.name());
                    break;
                default:
                    break;
            }

        }else{
            errorMessages.add("Assessment Type is Null");
        }
        return errorMessages;
    }
    
	@SuppressWarnings({ "unchecked" })
	private void validateResponses(Object object, List<String> errorMessages) {
    	try {
    		List<Map<String, Object>> responses = mapper.readValue(object.toString(), List.class);
    		if (null != responses && !responses.isEmpty()) {
    			for (Map<String, Object> response : responses) {
    				if (!isNotBlank(response, "values")) {
    					errorMessages.add("response must contain values");
    					break;
    				}
    				if (!isNotBlank(response, "score") && !isNotBlank(response, "mmc")) {
    					errorMessages.add("response must have either a score or missing micro-concepts (mmc)");
    					break;
    				}
    				Object objValues = response.get("values");
    				Map<String, Object> values = mapper.convertValue(objValues, Map.class);
    				if (null == values || values.isEmpty()) {
    					errorMessages.add("response must have atleast one value");
    					break;
    				}
    			}
    		}
    	} catch (Exception e) {
    		TelemetryManager.error("invalid responses definition: "+ e.getMessage(), e);
    		errorMessages.add("invalid responses definition");
    	}
    }

    @SuppressWarnings("rawtypes")
    public List<String> validateAssessmentItemSet(Node item) {
        List<String> errorMessages = new ArrayList<String>();
        Map<String, Object> metadata = item.getMetadata();
        if (null != metadata && !metadata.isEmpty()) {
            String type = getQuestionnaireType(item);
            if (!QuestionnaireType.isValidQuestionnaireType(type)) {
                errorMessages.add("Invalid Item Set type: " + type);
            } else {
                if (QuestionnaireType.materialised.name().equals(type)) {
                    try {
                        List list = mapper.readValue(mapper.writeValueAsString(metadata.get("memberIds")), List.class);
                        if (null == list || list.size() <= 0) {
                            errorMessages.add("Cannot create Item Set with no member items");
                        } else {
                        	Integer total = null;
                            if(metadata.get("total_items") instanceof Long)
                            	total = Integer.valueOf(((Long)metadata.get("total_items")).intValue());
                            else
                            	total = (Integer) metadata.get("total_items");
                            if (null == total) {
                                total = list.size();
                                metadata.put("total_items", total);
                            }
                            if (list.size() < total) {
                                errorMessages.add("Item Set should have atleast " + total + " assessment items");
                            }
                        }
                    } catch (Exception e) {
                        errorMessages.add("Invalid Item Set members list");
                    }
                    
                } else {
                    errorMessages.add("Unsupported Item Set type: " + type);
                }
            }
        }
        return errorMessages;
    }

    @SuppressWarnings("unchecked")
    private int checkMtfJson(Map<String, Object> metadata, List<String> errorMessages) {
        List<Object> option1 = new ArrayList<Object>();
        String lhsOptions = "lhs_options";
        String[] lhsKeys = new String[]{"value", "index"};
        String rhsOptions = "rhs_options";
        String[] rhsKeys = new String[]{"value"};
        if (null == metadata.get(lhsOptions)) {
            errorMessages.add("item " + lhsOptions + " is missing.");
        } else {
            try {
                List<Map<String, Object>> values = mapper.readValue((String) metadata.get(lhsOptions), List.class);
                int index = 0;
                for (Map<String, Object> value : values) {
                    for (String key : lhsKeys) {
                        if (!value.containsKey(key)) {
                            errorMessages.add(
                                    "invalid assessment item property: " + lhsOptions + ". " + key + " is missing.");
                            break;
                        }
                    }
                    if (!checkOptionValue(index, value, errorMessages))
                        break;
                    if (value.containsKey("index")) {
                        if (option1.contains(value.get("index"))) {
                            errorMessages.add("index should be unique.");
                            break;
                        }
                        option1.add(value.get("index"));
                    }
                    index += 1;
                }
                metadata.put(lhsOptions, values);
            } catch (Exception e) {
                errorMessages.add("invalid assessment item property: " + lhsOptions + ".");
            }
        }
        if (null == metadata.get(rhsOptions)) {
            errorMessages.add("item " + rhsOptions + " is missing.");
        } else {
            try {
                List<Map<String, Object>> values = mapper.readValue((String) metadata.get(rhsOptions), List.class);
                int index = 0;
                for (Map<String, Object> value : values) {
                    for (String key : rhsKeys) {
                        if (!value.containsKey(key)) {
                            errorMessages.add(
                                    "invalid assessment item property: " + rhsOptions + ". " + key + " is missing.");
                            break;
                        }
                    }
                    if (!checkOptionValue(index, value, errorMessages))
                        break;
                    index += 1;
                }
                metadata.put(rhsOptions, values);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessages.add("invalid assessment item property: " + rhsOptions + ".");
            }
        }
        return 0;
    }
    
    @SuppressWarnings("unchecked")
    private boolean checkOptionValue(int index, Map<String, Object> value, List<String> errorMessages) {
        boolean valid = true;
        if (null != value.get("value")) {
            Map<String, Object> valueMap = (Map<String, Object>) value.get("value");
            Object type = valueMap.get("type");
            if (null == type) {
                errorMessages.add(
                        "invalid option. Option 'type' cannot be null");
                valid = false;
            }
            if (!isNotBlank(valueMap, "resvalue")) {
            	if (isNotBlank(valueMap, "text"))
            		valueMap.put("resvalue", valueMap.get("text"));
            	else
            		valueMap.put("resvalue", index);
            }
            valueMap.put("resindex", index);
        }
        return valid;
    }
    
    private boolean isNotBlank(Map<String, Object> map, String key) {
    	if (null != map && StringUtils.isNotBlank(key) && map.containsKey(key)) {
    		if (null != map.get(key) && StringUtils.isNotBlank(map.get(key).toString()))
    			return true;
    	}
    	return false;
    }

    @SuppressWarnings("unchecked")
    private void checkJsonList(Map<String, Object> metadata, List<String> errorMessages, String propertyName,
            String[] keys, String itemType) {
        int numAnswers = 0;
        if (null != metadata.get("num_answers"))
            numAnswers = (int) metadata.get("num_answers");
        if (null == metadata.get(propertyName)) {
            errorMessages.add("item " + propertyName + " is missing.");
        } else {
            try {
                List<Map<String, Object>> values = mapper.readValue((String) metadata.get(propertyName), List.class);
                Integer answerCount = 0;
                int index = 0;
                for (Map<String, Object> value : values) {
                    for (String key : keys) {
                        if (!value.containsKey(key)) {
                            errorMessages.add(
                                    "invalid assessment item property: " + propertyName + ". " + key + " is missing.");
                            break;
                        }
                    }
                    if (null != itemType) {
                        if (null != value.get("score")) {
                            Integer score = null;
                            if(value.get("score") instanceof Long)
                            	score = Integer.valueOf(((Long)value.get("score")).intValue());
                            else
                            	score = (Integer) value.get("score");
                            if (score.intValue() > 0)
                                answerCount += 1;
                        } else if (null != value.get("answer")) {
                            Boolean answer = (Boolean) value.get("answer");
                            if (answer.booleanValue())
                                answerCount += 1;
                        }
                        if (!checkOptionValue(index, value, errorMessages))
                            break;
                        index += 1;
                    }
                }
                if (AssessmentItemType.mcq.name().equals(itemType)) {
                    if (answerCount < 1)
                        errorMessages.add("no option found with answer.");
                } else if (AssessmentItemType.mmcq.name().equals(itemType)) {
                    if (answerCount != numAnswers)
                        errorMessages.add("num_answers is not equals to no. of correct options");
                }
                metadata.put(propertyName, values);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessages.add("invalid assessment item property: " + propertyName + ".");
            }
        }
    }
}
