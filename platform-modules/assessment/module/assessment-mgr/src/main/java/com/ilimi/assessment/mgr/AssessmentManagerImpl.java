package com.ilimi.assessment.mgr;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.assessment.dto.ItemDTO;
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
import com.ilimi.graph.dac.model.Node;
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
        Node item = (Node) request.get(AssessmentAPIParams.questionnaire.name());
        if (null == item)
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_QUESTIONNAIRE.name(), "Questionnaire Object is blank");
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        List<String> assessmentErrors = validator.validateQuestionnaire(item);
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
                String type = validator.getQuestionnaireType(item);
                if(QuestionnaireType.materialised.name().equals(type)) {
                    Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
                    setReq.put(GraphDACParams.object_type.name(), "AssessmentItem");
                    setReq.put(GraphDACParams.members.name(), validator.getQuestionnaireMemberIds(item));
                    Response setRes = getResponse(setReq, LOGGER);
                    if(checkError(setRes)) {
                        return setRes;
                    } else {
                        item.getMetadata().remove("items");
                        // TODO - Create a Out Relation for the Set with relation type “associatedTo".
                    }
                    return setRes;
                } else if(QuestionnaireType.dynamic.name().equals(type)) {
                    // TODO - Create Out Relations for all Item Sets with relation type “associatedTo”.
                } else {
                    return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Invalid Questionnaire Type: "+type, ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
                }
                
                Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
                createReq.put(GraphDACParams.node.name(), item);
                Response createRes = getResponse(createReq, LOGGER);
                return createRes;
            }
        }
    }

    @Override
    public Response updateQuestionnaire(String id, String taxonomyId, Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response getQuestionnaire(Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response deleteQuestionnaire(Request request) {
        // TODO Auto-generated method stub
        return null;
    }

}
