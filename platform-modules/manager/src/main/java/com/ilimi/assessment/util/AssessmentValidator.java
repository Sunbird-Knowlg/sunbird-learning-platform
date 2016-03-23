package com.ilimi.assessment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ilimi.assessment.enums.AssessmentAPIParams;
import com.ilimi.assessment.enums.AssessmentItemType;
import com.ilimi.assessment.enums.QuestionnaireType;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;

@Component
public class AssessmentValidator extends BaseManager {

    private ObjectMapper mapper = new ObjectMapper();
    private static Logger LOGGER = LogManager.getLogger(AssessmentValidator.class.getName());

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

    @SuppressWarnings("unchecked")
    public List<String> getQuestionnaireItems(Node node) {
        Map<String, Object> metadata = node.getMetadata();
        List<String> memberIds = mapper.convertValue(metadata.get("items"), List.class);
        return memberIds;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getQuestionnaireItemSets(Node node) {
        List<Map<String, String>> values = null;
        Map<String, Object> metadata = node.getMetadata();
        try {
            values = mapper.readValue((String) metadata.get("item_sets"), List.class);
        } catch (Exception e) {
        }
        return values;
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
        if (AssessmentItemType.isValidAssessmentType(itemType)) {
            Map<String, Object> metadata = item.getMetadata();
            Integer numAnswers = null;
            if (null != metadata.get("num_answers"))
                numAnswers = (int) metadata.get("num_answers");
            if (null != metadata.get("hints"))
                checkJsonList(metadata, errorMessages, "hints",
                        new String[] { "order", "anchor", "content_type", "content", "start", "timing", "on_next" },
                        null);
            switch (AssessmentItemType.getAssessmentType(itemType)) {
            case mcq:
                checkJsonList(metadata, errorMessages, "options",
                        new String[] { "value" }, AssessmentItemType.mcq.name());
                break;
            case mmcq:
                checkJsonList(metadata, errorMessages, "options",
                        new String[] { "value" }, AssessmentItemType.mmcq.name());
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
                        new String[] { "value" }, AssessmentItemType.recognition.name());
                break;
            default:
                errorMessages.add("invalid assessment type: " + itemType);
                break;
            }
        } else {
            errorMessages.add("invalid assessment type: " + itemType);
        }
        return errorMessages;
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
                            Integer total = (Integer) metadata.get("total_items");
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
    public List<String> validateQuestionnaire(String taxonomyId, Node item) {
        List<String> errorMessages = new ArrayList<String>();
        Map<String, Object> metadata = item.getMetadata();
        String type = getQuestionnaireType(item);
        if (!QuestionnaireType.isValidQuestionnaireType(type)) {
            errorMessages.add("invalid questionnaire type" + type);
        } else {
            if (QuestionnaireType.materialised.name().equals(type)) {
                try {
                    @SuppressWarnings("rawtypes")
                    List list = mapper.readValue(mapper.writeValueAsString(metadata.get("items")), List.class);
                    Integer total = (Integer) metadata.get("total_items");
                    if (list.size() < total) {
                        errorMessages.add("Questionnaire has insufficient assessment items.");
                    }
                } catch (Exception e) {
                    errorMessages.add("invalid items array list.");
                }
            } else if (QuestionnaireType.dynamic.name().equals(type)) {
                List<String> dynamicErrors = new ArrayList<String>();
                checkJsonList(metadata, dynamicErrors, "item_sets", new String[] { "id", "count" }, null);
                // TODO: check for ItemSet exist and the count is s
                List<Map<String, Object>> values = null;
                try {
                    values = (List<Map<String, Object>>) mapper.readValue((String) metadata.get("item_sets"),
                            List.class);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                boolean check = true;
                for (Map<String, Object> value : values) {
                    Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getSet");
                    setReq.put(GraphDACParams.object_type.name(), "ItemSet");
                    setReq.put(GraphDACParams.collection_id.name(), value.get("id"));
                    Response setRes = getResponse(setReq, LOGGER);
                    if (setRes.getParams().getStatus().equals("failed")) {
                        check = false;
                        errorMessages.add("Set id " + value.get("id") + ": not found");
                        break;
                    }
                }
                if (check) {
                    for (Map<String, Object> value : values) {
                        Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER,
                                "getSetCardinality");
                        setReq.put(GraphDACParams.object_type.name(), "ItemSet");
                        setReq.put(GraphDACParams.collection_id.name(), value.get("id"));
                        Response setRes = getResponse(setReq, LOGGER);
                        int count = (Integer) value.get("count");
                        Long cardinality = (Long) setRes.getResult().get("cardinality");
                        if (count > cardinality) {
                            errorMessages.add("item_set " + value.get("id") + " : does not contain sufficient items");
                            break;
                        }
                    }
                }
                if (dynamicErrors.size() == 0) {
                    try {
                        Integer total = (Integer) metadata.get("total_items");
                        List<Map<String, Object>> list = (List<Map<String, Object>>) mapper
                                .readValue((String) metadata.get("item_sets"), List.class);

                        Integer criteriaTotal = 0;
                        for (Map<String, Object> itemSet : list) {
                            if (!(itemSet.get("count") instanceof Integer)) {
                                errorMessages.add("item_sets property count should be a Numeric value");
                                break;
                            }
                            int count = (int) itemSet.get("count");
                            criteriaTotal += count;
                        }
                        if (criteriaTotal != total) {
                            errorMessages.add("Questionnaire has insufficient assessment items (count).");
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
    private int checkMtfJson(Map<String, Object> metadata, List<String> errorMessages) {
        List<Object> option1 = new ArrayList<Object>();
        List<Object> option2 = new ArrayList<Object>();
        String lhsOptions = "lhs_options";
        String[] lhsKeys = new String[]{"value", "index"};
        String rhsOptions = "rhs_options";
        String[] rhsKeys = new String[]{"value"};
        if (null == metadata.get(lhsOptions)) {
            errorMessages.add("item " + lhsOptions + " is missing.");
        } else {
            try {
                List<Map<String, Object>> values = mapper.readValue((String) metadata.get(lhsOptions), List.class);
                for (Map<String, Object> value : values) {
                    for (String key : lhsKeys) {
                        if (!value.containsKey(key)) {
                            errorMessages.add(
                                    "invalid assessment item property: " + lhsOptions + ". " + key + " is missing.");
                            break;
                        }
                    }
                    if (!checkOptionValue(value, errorMessages))
                        break;
                    if (value.containsKey("index")) {
                        if (option1.contains(value.get("index"))) {
                            errorMessages.add("index should be unique.");
                            break;
                        }
                        option1.add(value.get("index"));
                    }
                }
            } catch (Exception e) {
                errorMessages.add("invalid assessment item property: " + lhsOptions + ".");
            }
        }
        if (null == metadata.get(rhsOptions)) {
            errorMessages.add("item " + rhsOptions + " is missing.");
        } else {
            try {
                List<Map<String, Object>> values = mapper.readValue((String) metadata.get(rhsOptions), List.class);
                for (Map<String, Object> value : values) {
                    for (String key : rhsKeys) {
                        if (!value.containsKey(key)) {
                            errorMessages.add(
                                    "invalid assessment item property: " + rhsOptions + ". " + key + " is missing.");
                            break;
                        }
                    }
                    if (!checkOptionValue(value, errorMessages))
                        break;
                    if (value.containsKey("answer")) {
                        if (option2.contains(value.get("answer"))) {
                            errorMessages.add("answer should be unique.");
                            return 0;
                        }
                        option2.add(value.get("answer"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessages.add("invalid assessment item property: " + rhsOptions + ".");
            }
        }
        return 0;
    }
    
    @SuppressWarnings("unchecked")
    private boolean checkOptionValue(Map<String, Object> value, List<String> errorMessages) {
        boolean valid = true;
        if (null != value.get("value")) {
            Map<String, Object> valueMap = (Map<String, Object>) value.get("value");
            Object type = valueMap.get("type");
            Object asset = valueMap.get("asset");
            if (null == type) {
                errorMessages.add(
                        "invalid option. Option 'type' cannot be null");
                valid = false;
            }
            if (null == asset) {
                asset = value.get("answer");
            }
            if (null == asset) {
                errorMessages.add(
                        "invalid option. Option 'value' cannot be null");
                valid = false;
            } else {
                valueMap.put("asset", asset);
            }
        }
        return valid;
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
                            Integer score = (Integer) value.get("score");
                            if (score.intValue() > 0)
                                answerCount += 1;
                        } else if (null != value.get("answer")) {
                            Boolean answer = (Boolean) value.get("answer");
                            if (answer.booleanValue())
                                answerCount += 1;
                        }
                        if (!checkOptionValue(value, errorMessages))
                            break;
                    }
                }
                if (AssessmentItemType.mcq.name().equals(itemType)) {
                    if (answerCount < 1)
                        errorMessages.add("no option found with answer.");
                } else if (AssessmentItemType.mmcq.name().equals(itemType)) {
                    if (answerCount != numAnswers)
                        errorMessages.add("num_answers is not equals to no. of correct options");
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessages.add("invalid assessment item property: " + propertyName + ".");
            }
        }
    }

    public String getQuestionnaireSetId(Node node) {
        String setId = null;
        for (Relation relation : node.getOutRelations()) {
            if (SystemNodeTypes.SET.name().equals(relation.getEndNodeType())) {
                setId = relation.getEndNodeId();
                break;
            }
        }
        return setId;
    }

    public void compareMembers(List<String> inputMembers, List<String> existingMembers, List<String> addIds,
            List<String> removeIds) {
        if (null != inputMembers) {
            for (String member : inputMembers) {
                if (existingMembers.contains(member)) {
                    existingMembers.remove(member);
                } else {
                    addIds.add(member);
                }
            }
            removeIds.addAll(existingMembers);
        }
    }

    public List<String> validateAssessmentItemSet(Request request) {
        List<String> errorMessages = new ArrayList<String>();
        try {
            @SuppressWarnings("rawtypes")
            Map map = mapper.readValue(
                    mapper.writeValueAsString(request.get(AssessmentAPIParams.assessment_search_criteria.name())),
                    Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) map.get("metadata");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> values = (List<Map<String, Object>>) metadata.get("filters");
            for (Map<String, Object> value : values) {
                if (value.get("property").equals("")) {
                    errorMessages.add(value.get("property") + " can not be empty string");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessages.add("Invalid Assessment Search Criteira");
        }
        return errorMessages;
    }

}
