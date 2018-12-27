package org.ekstep.content.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class RetireManager extends DummyBaseContentManager {

    /**
     * @param contentId
     * @return
     */
    public Response retire(String contentId) {
        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                    "Content Object Id is blank.");
        Response response = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(response))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                    "Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");
        Node node = (Node) response.get(GraphDACParams.node.name());
        Set<String> identifiers = new HashSet<>();
        populateIdsToRetire(node, identifiers, getDefinition(TAXONOMY_ID, CONTENT_IMAGE_OBJECT_TYPE), getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE));
        Map<String, Object> params = new HashMap<>();
        params.put("status", "Retired");
        if(identifiers.isEmpty()) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_RETIRE.name(),
                    "Content is already Retired.");
        }
        else {
            response = updateDataNodes(params, new ArrayList<>(identifiers), TAXONOMY_ID);
            if(checkError(response)) {
                return response;
            }else {
                deleteHierarchy(new ArrayList<>(identifiers));
                Response responseNode = getDataNode(TAXONOMY_ID, contentId);
                if(checkError(responseNode)) {
                    throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                            "Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");
                } else {
                    node = (Node) responseNode.get("node");
                    Response res = getSuccessResponse();
                    res.put(ContentAPIParams.node_id.name(), node.getIdentifier());
                    res.put(ContentAPIParams.versionKey.name(), node.getMetadata().get("versionKey"));
                    return res;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void populateIdsToRetire(Node node, Set<String> identifiers, DefinitionDTO contentDef, DefinitionDTO contentImgDef) {
        DefinitionDTO definition;
        if(StringUtils.endsWithIgnoreCase(node.getIdentifier(), DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
            definition = contentImgDef;
        } else {
            Response responseImageNode = getDataNode(TAXONOMY_ID, getImageId(node.getIdentifier()));
            if(!checkError(responseImageNode) && !identifiers.contains(node.getIdentifier()))
                populateIdsToRetire((Node) responseImageNode.get(GraphDACParams.node.name()), identifiers, contentDef, contentImgDef);
            definition = contentDef;
        }
        if(!StringUtils.equalsIgnoreCase("Retired", (String) node.getMetadata().get("status")) && !identifiers.contains(node.getIdentifier())) {
            identifiers.add(node.getIdentifier());
            Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
            Optional.ofNullable((List<NodeDTO>) contentMap.get("children")).ifPresent(children -> {
                if (!children.isEmpty()) {
                    children.forEach(dto -> {
                        Response responseNode = getDataNode(TAXONOMY_ID, dto.getIdentifier());
                        Node childNode = (Node) responseNode.get(GraphDACParams.node.name());
                        if ("Parent".equals(childNode.getMetadata().get("visibility")) && !identifiers.contains(childNode.getIdentifier()))
                            populateIdsToRetire(childNode, identifiers, contentDef, contentImgDef);
                    });
                }
            });
        }
    }

    /**
     * @param map
     * @param idList
     * @param graphId
     * @return
     */
    private Response updateDataNodes(Map<String, Object> map, List<String> idList, String graphId) {
        Response response = new Response();
        TelemetryManager.log("Getting Update Node Request For Node ID: " + idList);
        Request updateReq = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "updateDataNodes");
        updateReq.put(GraphDACParams.node_ids.name(), idList);
        updateReq.put(GraphDACParams.metadata.name(), map);
        TelemetryManager.log("Updating DialCodes for :" + idList);
        response = getResponse(updateReq);
        TelemetryManager.log("Returning Node Update Response.");
        return response;
    }

}
