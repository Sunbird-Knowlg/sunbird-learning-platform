package com.ilimi.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;

@Component
public class ContentManagerImpl extends BaseManager implements IContentManager {

    private static Logger LOGGER = LogManager.getLogger(IContentManager.class.getName());
    
    @Override
    public Response create(String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType)) 
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");
        Node item = (Node) request.get(ContentAPIParams.worksheet.name());
        if (null == item)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), objectType+" Object is blank");
        item.setObjectType(objectType);
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        // TODO: check whether do we need content level validation.
        if(checkError(validateRes)) {
            return validateRes;
        } else {
            Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
            createReq.put(GraphDACParams.node.name(), item);
            Response createRes = getResponse(createReq, LOGGER);
            return createRes;
        }
    }

    @Override
    public Response findAll(String taxonomyId, String objectType, Integer offset, Integer limit, String[] gfields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response find(String id, String taxonomyId, String objectType, String[] fields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Content Object Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        if(!objectType.equalsIgnoreCase(node.getObjectType())) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_OBJECT_TYPE.name(), "Invalid object type of the content for given id: "+id);
        }
        return getNodeRes;
    }

    @Override
    public Response update(String id, String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Content Object Id is blank");
        if (StringUtils.isBlank(objectType)) 
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");        
        Node item = (Node) request.get(ContentAPIParams.worksheet.name());
        if (null == item)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), objectType+" Object is blank");
        item.setIdentifier(id);
        item.setObjectType(objectType);
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        // TODO: check whether do we need content level validation.        
        if(checkError(validateRes)) {
            return validateRes;
        } else {
            Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
            updateReq.put(GraphDACParams.node.name(), item);
            updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
            Response updateRes = getResponse(updateReq, LOGGER);
            return updateRes;
        }
    }

    @Override
    public Response delete(String id, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Content Object Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode", GraphDACParams.node_id.name(), id);
        return getResponse(request, LOGGER);
    }

}
