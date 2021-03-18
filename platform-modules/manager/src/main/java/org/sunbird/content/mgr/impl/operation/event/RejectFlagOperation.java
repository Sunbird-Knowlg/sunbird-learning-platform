package org.sunbird.content.mgr.impl.operation.event;

import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.Map;
import java.util.HashMap;

public class RejectFlagOperation extends BaseContentManager {

    public Response rejectFlag(String contentId) throws Exception {

        TelemetryManager.log("RejectFlagOperation:rejectFlag: Get data node for content: " + contentId);
        Response nodeResponse = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(nodeResponse))
            return nodeResponse;

        Node node = (Node) nodeResponse.getResult().get(ContentAPIParams.node.name());
        String objectType = node.getObjectType();
        if (VALID_FLAG_OBJECT_TYPES.contains(objectType)) {
            Map<String, Object> metadata = node.getMetadata();
            String status = (String) metadata.get(ContentAPIParams.status.name());
            if (ContentAPIParams.Flagged.name().equalsIgnoreCase(status)) {
                TelemetryManager.log("RejectFlagOperation:rejectFlag: Get image data node for content: " + contentId);
                Response imageNodeResponse = getDataNode(TAXONOMY_ID, contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
                if (checkError(imageNodeResponse)) {
                    Map request = new HashMap();
                    request.put(ContentWorkflowPipelineParams.flagReasons.name(), null);
                    request.put(ContentAPIParams.versionKey.name(), metadata.get(ContentAPIParams.versionKey.name()));
                    request.put(ContentAPIParams.status.name(), ContentAPIParams.Live.name());
                    request.put(ContentAPIParams.objectType.name(), objectType);
                    request.put(ContentAPIParams.identifier.name(), contentId);

                    TelemetryManager.log("RejectFlagOperation:rejectFlag: Update data node for content: " + contentId);
                    DefinitionDTO definition = getDefinition(TAXONOMY_ID, objectType);
                    Node domainObj = ConvertToGraphNode.convertToGraphNode(request, definition, null);
                    return updateNode(contentId, objectType, domainObj);

                } else {
                    return ERROR("ERR_CONTENT_ALREADY_ACCEPTED", "Content " + contentId + " - flag is already accepted", ResponseCode.CLIENT_ERROR);
                }
            } else {
                return ERROR("ERR_CONTENT_NOT_FLAGGED", "Content " + contentId + " is not flagged to reject", ResponseCode.CLIENT_ERROR);
            }
        } else {
            return ERROR("ERR_NODE_NOT_FOUND", objectType + " " + contentId + " not found", ResponseCode.RESOURCE_NOT_FOUND);
        }
    }
}
