package org.ekstep.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.assessment.mgr.IAssessmentManager;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.ekstep.taxonomy.mgr.IObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ObjectManagerImpl extends BaseContentManager implements IObjectManager {

    @Override
    public Response create(String objectType, Map<String, Object> request) throws Exception {
        DefinitionDTO definition = validateDefinition(objectType);
        Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
        node.setObjectType(definition.getObjectType());
        node.setGraphId(TAXONOMY_ID);
        Response response = createDataNode(node);
        return response;
    }

    @Override
    public Response update(String objectType, String id, Map<String, Object> request) throws Exception {
        DefinitionDTO definition = validateDefinition(objectType);
        Response readResponse = getDataNode(TAXONOMY_ID, id);
        if(checkError(readResponse))
            return readResponse;

        Node graphNode = (Node) readResponse.get(GraphDACParams.node.name());

        if(StringUtils.equalsIgnoreCase("Retired", (String) graphNode.getMetadata().get("status"))) {
            throw new ResourceNotFoundException("ERR_OBJECT_NOT_FOUND",
                    "Object not found with id: " + id);
        }
        Node domainObj = ConvertToGraphNode.convertToGraphNode(request, definition, graphNode);

        Response updateResponse = updateNode(id, StringUtils.capitalize(objectType), domainObj);
        return updateResponse;
    }

    @Override
    public Response read(String objectType, String id) {
        validateDefinition(objectType);
        Response dataNodeResponse = getDataNode(TAXONOMY_ID, id);
        if(checkError(dataNodeResponse)){
            return dataNodeResponse;
        }
        Node graphNode = (Node) dataNodeResponse.get(GraphDACParams.node.name());
        if(StringUtils.equalsIgnoreCase("Retired", (String) graphNode.getMetadata().get("status"))) {
            throw new ResourceNotFoundException("ERR_OBJECT_NOT_FOUND",
                    "Object not found with id: " + id);
        }
        Response readResponse = OK();
        readResponse.put(objectType, graphNode);
        return readResponse;
    }

    @Override
    public Response delete(String objectType, String id) {
        validateDefinition(objectType);
        Response readResponse = getDataNode(TAXONOMY_ID, id);
        if(checkError(readResponse))
            return readResponse;

        Node graphNode = (Node) readResponse.get(GraphDACParams.node.name());
        if(StringUtils.equalsIgnoreCase("Retired", (String) graphNode.getMetadata().get("status"))) {
            throw new ResourceNotFoundException("ERR_OBJECT_NOT_FOUND",
                    "Object not found with id: " + id);
        }

        graphNode.getMetadata().put("status", "Retired");
        Response updateResponse = updateNode(id, StringUtils.capitalize(objectType), graphNode);
        return updateResponse;
    }

    private DefinitionDTO validateDefinition(String objectType) {
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, StringUtils.capitalize(objectType));
        if(null == definition){
            throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "Invalid Object : " + objectType);
        }
        return definition;
    }

}
