package com.ilimi.assessment.mgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.assessment.dto.ItemDTO;
import com.ilimi.assessment.dto.QuestionnaireDTO;
import com.ilimi.assessment.enums.AssessmentAPIParams;
import com.ilimi.assessment.enums.AssessmentErrorCodes;
import com.ilimi.assessment.enums.QuestionnaireType;
import com.ilimi.assessment.util.AssessmentValidator;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.node.MetadataDefinition;

@Component
public class AssessmentManagerImpl extends BaseManager implements IAssessmentManager {

    private static Logger LOGGER = LogManager.getLogger(IAssessmentManager.class.getName());
    
    @Autowired
    private AssessmentValidator validator;
    
    @SuppressWarnings("unchecked")
    @Override
    public Response createAssessmentItem(String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Node item = (Node) request.get(AssessmentAPIParams.assessment_item.name());
        if (null == item)
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(), "AssessmentItem Object is blank");
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        List<String> assessmentErrors = validator.validateAssessmentItem(item);
        if(checkError(validateRes)) {
            if(assessmentErrors.size() > 0) {
                List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
                messages.addAll(assessmentErrors);
            }
            return validateRes;
        } else {
            if(assessmentErrors.size() > 0) {
                return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "AssessmentItem validation failed", ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
            } else {
                Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
                createReq.put(GraphDACParams.node.name(), item);
                Response createRes = getResponse(createReq, LOGGER);
                if (checkError(createRes)) {
                    return createRes;
                } else {
                    List<MetadataDefinition> newDefinitions = (List<MetadataDefinition>) request.get(AssessmentAPIParams.metadata_definitions.name());
                    if (validateRequired(newDefinitions)) {
                        Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                        defRequest.put(GraphDACParams.object_type.name(), item.getObjectType());
                        defRequest.put(GraphDACParams.metadata_definitions.name(), newDefinitions);
                        Response defResponse = getResponse(defRequest, LOGGER);
                        if (checkError(defResponse)) {
                            return defResponse;
                        }
                    }
                }
                return createRes;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Response updateAssessmentItem(String id, String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(), "AssessmentItem Id is blank");
        Node item = (Node) request.get(AssessmentAPIParams.assessment_item.name());
        if (null == item)
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(), "AssessmentItem Object is blank");
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        List<String> assessmentErrors = validator.validateAssessmentItem(item);
        if(checkError(validateRes)) {
            if(assessmentErrors.size() > 0) {
                List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
                messages.addAll(assessmentErrors);
            }
            return validateRes;
        } else {
            if(assessmentErrors.size() > 0) {
                return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Node validation failed", ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
            } else {
                if(null == item.getIdentifier()) item.setIdentifier(id);
                Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
                updateReq.put(GraphDACParams.node.name(), item);
                updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
                Response updateRes = getResponse(updateReq, LOGGER);
                if (checkError(updateRes)) {
                    return updateRes;
                } else {
                    List<MetadataDefinition> newDefinitions = (List<MetadataDefinition>) request.get(AssessmentAPIParams.metadata_definitions.name());
                    if (validateRequired(newDefinitions)) {
                        Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                        defRequest.put(GraphDACParams.object_type.name(), item.getObjectType());
                        defRequest.put(GraphDACParams.metadata_definitions.name(), newDefinitions);
                        Response defResponse = getResponse(defRequest, LOGGER);
                        if (checkError(defResponse)) {
                            return defResponse;
                        }
                    }
                }
                return updateRes; 
            }
        }
    }

    @Override
    public Response searchAssessmentItems(Request request) {
        
        return null;
    }

    @Override
    public Response deleteAssessmentItem(String id, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(), "AssessmentItem Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode", GraphDACParams.node_id.name(), id);
        return getResponse(request, LOGGER);
    }

    @Override
    public Response getAssessmentItem(String id, String taxonomyId, String[] ifields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(), "AssessmentItem Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        if (null != node) {
            ItemDTO dto = new ItemDTO(node, ifields);
            response.put(AssessmentAPIParams.assessment_item.name(), dto);
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response createQuestionnaire(String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Node node = (Node) request.get(AssessmentAPIParams.questionnaire.name());
        if (null == node)
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_QUESTIONNAIRE.name(), "Questionnaire Object is blank");
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), node);
        Response validateRes = getResponse(validateReq, LOGGER);
        List<String> assessmentErrors = validator.validateQuestionnaire(node);
        if(checkError(validateRes)) {
            if(assessmentErrors.size() > 0) {
                List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
                messages.addAll(assessmentErrors);
            }
            return validateRes;
        } else {
            if(assessmentErrors.size() > 0) {
                return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Questionnaire validation failed", ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
            } else {
                String type = validator.getQuestionnaireType(node);
                if(QuestionnaireType.materialised.name().equals(type)) {
                    Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
                    setReq.put(GraphDACParams.object_type.name(), "AssessmentItem");
                    setReq.put(GraphDACParams.members.name(), validator.getQuestionnaireItems(node));
                    Response setRes = getResponse(setReq, LOGGER);
                    if(checkError(setRes)) {
                        return setRes;
                    } else {
                        node.getMetadata().remove("items");
                        Relation relation = new Relation();
                        relation.setEndNodeId((String)setRes.get(GraphDACParams.set_id.name()));
                        relation.setRelationType(RelationTypes.ASSOCIATED_TO.relationName());
                        Map<String, Object> metadata = new HashMap<String, Object>();
                        metadata.put("count", node.getMetadata().get("total_items"));
                        relation.setMetadata(metadata);
                        node.getOutRelations().add(relation);
                    }
                } else if(QuestionnaireType.dynamic.name().equals(type)) {
                    List<Map<String, String>> setCriteria = validator.getQuestionnaireItemSets(node);
                    for(Map<String, String> criteria : setCriteria) {
                        Relation relation = new Relation();
                        relation.setEndNodeId(criteria.get("id"));
                        relation.setRelationType(RelationTypes.ASSOCIATED_TO.relationName());
                        Map<String, Object> metadata = new HashMap<String, Object>();
                        metadata.put("count", criteria.get("count"));
                        relation.setMetadata(metadata);
                        node.getOutRelations().add(relation);
                    }
                } else {
                    return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Invalid Questionnaire Type: "+type, ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
                }
                Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
                createReq.put(GraphDACParams.node.name(), node);
                Response createRes = getResponse(createReq, LOGGER);
                return createRes;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response updateQuestionnaire(String id, String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_QUESTIONNAIRE_ID.name(), "Questionnaire Id is blank");
        Node node = (Node) request.get(AssessmentAPIParams.questionnaire.name());
        if (null == node)
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_QUESTIONNAIRE.name(), "Questionnaire Object is blank");
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), node);
        Response validateRes = getResponse(validateReq, LOGGER);
        List<String> assessmentErrors = validator.validateQuestionnaire(node);
        if(checkError(validateRes)) {
            if(assessmentErrors.size() > 0) {
                List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
                messages.addAll(assessmentErrors);
            }
            return validateRes;
        } else {
            if(assessmentErrors.size() > 0) {
                return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Questionnaire validation failed", ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
            } else {
                String type = validator.getQuestionnaireType(node);
                if(QuestionnaireType.materialised.name().equals(type)) {
                    List<String> inputMembers = validator.getQuestionnaireItems(node);
                    
                    Request getNodeReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
                    getNodeReq.put(GraphDACParams.get_tags.name(), true);
                    Response getNodeRes = getResponse(getNodeReq, LOGGER);
                    Node qrNode = (Node) getNodeRes.get(GraphDACParams.node.name());
                    
                    Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getCollectionMembers");
                    setReq.put(GraphDACParams.collection_type.name(), SystemNodeTypes.SET.name());
                    String setId = validator.getQuestionnaireSetId(qrNode);
                    setReq.put(GraphDACParams.collection_id.name(), setId);
                    Response setRes = getResponse(setReq, LOGGER);
                    List<String> existingMembers = (List<String>) setRes.get(GraphDACParams.members.name());
                    List<String> removeIds = new ArrayList<String>();
                    List<String> addIds = new ArrayList<String>();
                    validator.compareMembers(inputMembers, existingMembers, addIds, removeIds);
                    if(addIds.size() > 0) {
                        for(String addId: addIds) {
                            Request addMemReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "addMember");
                            setReq.put(GraphDACParams.collection_type.name(), SystemNodeTypes.SET.name());
                            setReq.put(GraphDACParams.collection_id.name(), setId);
                            setReq.put(GraphDACParams.member_id.name(), addId);
                            getResponse(addMemReq, LOGGER);
                        }
                    }
                    
                    if(removeIds.size() > 0) {
                        for(String removeId: removeIds) {
                            Request addMemReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "removeMember");
                            setReq.put(GraphDACParams.collection_type.name(), SystemNodeTypes.SET.name());
                            setReq.put(GraphDACParams.collection_id.name(), setId);
                            setReq.put(GraphDACParams.member_id.name(), removeId);
                            getResponse(addMemReq, LOGGER);
                        }
                    }
                    node.getMetadata().remove("items");
                    Relation relation = new Relation();
                    relation.setEndNodeId((String)setRes.get(GraphDACParams.set_id.name()));
                    relation.setRelationType(RelationTypes.ASSOCIATED_TO.relationName());
                    Map<String, Object> metadata = new HashMap<String, Object>();
                    metadata.put("count", node.getMetadata().get("total_items"));
                    relation.setMetadata(metadata);
                    node.getOutRelations().add(relation);
                } else if(QuestionnaireType.dynamic.name().equals(type)) {
                    List<Map<String, String>> setCriteria = validator.getQuestionnaireItemSets(node);
                    for(Map<String, String> criteria : setCriteria) {
                        Relation relation = new Relation();
                        relation.setEndNodeId(criteria.get("id"));
                        relation.setRelationType(RelationTypes.ASSOCIATED_TO.relationName());
                        Map<String, Object> metadata = new HashMap<String, Object>();
                        metadata.put("count", criteria.get("count"));
                        relation.setMetadata(metadata);
                        node.getOutRelations().add(relation);
                    }
                } else {
                    return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Invalid Questionnaire Type: "+type, ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
                }
            }
        }
        if(null == node.getIdentifier()) node.setIdentifier(id);
        Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), node);
        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
        Response updateRes = getResponse(updateReq, LOGGER);
        return updateRes;
    }

    @Override
    public Response getQuestionnaire(String id, String taxonomyId, String[] qrfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_QUESTIONNAIRE_ID.name(), "Questionnaire Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        if (null != node) {
            QuestionnaireDTO dto = new QuestionnaireDTO(node, qrfields);
            response.put(AssessmentAPIParams.questionnaire.name(), dto);
        }
        return response;
    }

    @Override
    public Response deleteQuestionnaire(String id, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_QUESTIONNAIRE_ID.name(), "Questionnaire Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode", GraphDACParams.node_id.name(), id);
        return getResponse(request, LOGGER);
    }

}
