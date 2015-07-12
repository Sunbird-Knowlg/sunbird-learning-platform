package com.ilimi.assessment.mgr;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.assessment.dto.ItemDTO;
import com.ilimi.assessment.enums.AssessmentAPIParams;
import com.ilimi.assessment.enums.AssessmentErrorCodes;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.MetadataDefinition;

public class AssessmentManagerImpl extends BaseManager implements IAssessmentManager {

    private static Logger LOGGER = LogManager.getLogger(IAssessmentManager.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public Response createAssessmentItem(String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Node item = (Node) request.get(AssessmentAPIParams.item.name());
        if (null == item)
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(), "AssessmentItem Object is blank");
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
    
    @SuppressWarnings("unchecked")
    @Override
    public Response updateAssessmentItem(String id, String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(), "AssessmentItem Id is blank");
        Node item = (Node) request.get(AssessmentAPIParams.item.name());
        if (null == item)
            throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(), "AssessmentItem Object is blank");
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
            response.put(AssessmentAPIParams.item.name(), dto);
        }
        return response;
    }

}
