package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.taxonomy.dto.GameDTO;
import com.ilimi.taxonomy.dto.MediaDTO;
import com.ilimi.taxonomy.enums.LearningObjectAPIParams;
import com.ilimi.taxonomy.enums.LearningObjectErrorCodes;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.ILearningObjectManager;

@Component
public class LearningObjectManagerImpl extends BaseManager implements ILearningObjectManager {

    protected static final String OBJECT_TYPE = "Game";

    private static Logger LOGGER = LogManager.getLogger(ILearningObjectManager.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public Response findAll(String taxonomyId, String objectType, Integer offset, Integer limit, String[] gfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            objectType = OBJECT_TYPE;
        LOGGER.info("Find All Learning Objects : " + taxonomyId + ", ObjectType: " + objectType);
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.sort(new Sort(PARAM_STATUS, Sort.SORT_ASC));
        if (null != offset && offset.intValue() >= 0)
            sc.setStartPosition(offset);
        if (null != limit && limit.intValue() > 0)
            sc.setResultSize(limit);
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes", GraphDACParams.search_criteria.name(),
                sc);
        request.put(GraphDACParams.get_tags.name(), true);
        Response findRes = getResponse(request, LOGGER);
        Response response = copyResponse(findRes);
        if (checkError(response))
            return response;
        else {
            List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
            if (null != nodes && !nodes.isEmpty()) {
                if (null != gfields && gfields.length > 0) {
                    for (Node node : nodes) {
                        setMetadataFields(node, gfields);
                    }
                }
                response.put(LearningObjectAPIParams.learning_objects.name(), nodes);
            }
            Request countReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodesCount",
                    GraphDACParams.search_criteria.name(), sc);
            Response countRes = getResponse(countReq, LOGGER);
            if (checkError(countRes)) {
                return countRes;
            } else {
                Long count = (Long) countRes.get(GraphDACParams.count.name());
                response.put(GraphDACParams.count.name(), count);
            }
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response find(String id, String taxonomyId, String[] gfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(LearningObjectErrorCodes.ERR_LOB_BLANK_LEARNING_OBJECT_ID.name(), "Learning Object Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        if (null != node) {
            if (StringUtils.equalsIgnoreCase(OBJECT_TYPE, node.getObjectType())) {
                GameDTO dto = new GameDTO(node, gfields);
                List<String> mediaIds = dto.screenShots();
                if (null != mediaIds && !mediaIds.isEmpty()) {
                    Request mediaReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNodes",
                            GraphDACParams.node_ids.name(), mediaIds);
                    Response mediaRes = getResponse(mediaReq, LOGGER);
                    List<Node> mediaNodes = (List<Node>) mediaRes.get(GraphDACParams.node_list.name());
                    if (validateRequired(mediaNodes)) {
                        List<MediaDTO> screenshots = new ArrayList<MediaDTO>();
                        for (Node mediaNode : mediaNodes) {
                            MediaDTO media = new MediaDTO(mediaNode);
                            screenshots.add(media);
                        }
                        dto.setScreenshots(screenshots);
                    }
                }
                response.put(LearningObjectAPIParams.learning_object.name(), dto);
            } else {
                response.put(LearningObjectAPIParams.learning_object.name(), node);
            }
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response create(String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Node lob = (Node) request.get(LearningObjectAPIParams.learning_object.name());
        if (null == lob)
            throw new ClientException(LearningObjectErrorCodes.ERR_LOB_BLANK_LEARNING_OBJECT.name(), "Learning Object is blank");
        Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
        createReq.put(GraphDACParams.node.name(), lob);
        Response createRes = getResponse(createReq, LOGGER);
        if (checkError(createRes)) {
            return createRes;
        } else {
            List<MetadataDefinition> newDefinitions = (List<MetadataDefinition>) request.get(TaxonomyAPIParams.metadata_definitions.name());
            if (validateRequired(newDefinitions)) {
                Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                defRequest.put(GraphDACParams.object_type.name(), lob.getObjectType());
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
    public Response createMedia(String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Node lob = (Node) request.get(LearningObjectAPIParams.media.name());
        if (null == lob)
            throw new ClientException(LearningObjectErrorCodes.ERR_LOB_BLANK_MEDIA_OBJECT.name(), "Media Object is blank");
        Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
        createReq.put(GraphDACParams.node.name(), lob);
        Response createRes = getResponse(createReq, LOGGER);
        if (checkError(createRes)) {
            return createRes;
        } else {
            List<MetadataDefinition> newDefinitions = (List<MetadataDefinition>) request.get(TaxonomyAPIParams.metadata_definitions.name());
            if (validateRequired(newDefinitions)) {
                Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                defRequest.put(GraphDACParams.object_type.name(), lob.getObjectType());
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
    public Response update(String id, String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(LearningObjectErrorCodes.ERR_LOB_BLANK_LEARNING_OBJECT_ID.name(), "Learning Object Id is blank");
        Node lob = (Node) request.get(LearningObjectAPIParams.learning_object.name());
        if (null == lob)
            throw new ClientException(LearningObjectErrorCodes.ERR_LOB_BLANK_LEARNING_OBJECT.name(), "Learning Object is blank");
        Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), lob);
        updateReq.put(GraphDACParams.node_id.name(), lob.getIdentifier());
        Response updateRes = getResponse(updateReq, LOGGER);
        if (checkError(updateRes)) {
            return updateRes;
        } else {
            List<MetadataDefinition> newDefinitions = (List<MetadataDefinition>) request.get(TaxonomyAPIParams.metadata_definitions.name());
            if (validateRequired(newDefinitions)) {
                Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                defRequest.put(GraphDACParams.object_type.name(), lob.getObjectType());
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
    public Response delete(String id, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(LearningObjectErrorCodes.ERR_LOB_BLANK_LEARNING_OBJECT_ID.name(), "Learning Object Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode", GraphDACParams.node_id.name(), id);
        return getResponse(request, LOGGER);
    }

    @Override
    public Response deleteRelation(String startLobId, String relationType, String endLobId, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(startLobId) || StringUtils.isBlank(relationType) || StringUtils.isBlank(endLobId))
            throw new ClientException(LearningObjectErrorCodes.ERR_LOB_UPDATE_OBJECT.name(),
                    "Start Lob Id, Relation Type and End Lob Id are required to delete relation");
        Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER, "removeRelation");
        request.put(GraphDACParams.start_node_id.name(), startLobId);
        request.put(GraphDACParams.relation_type.name(), relationType);
        request.put(GraphDACParams.end_node_id.name(), endLobId);
        return getResponse(request, LOGGER);
    }

}
