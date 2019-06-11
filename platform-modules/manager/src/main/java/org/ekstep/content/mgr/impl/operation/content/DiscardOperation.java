package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.util.RequestValidatorUtil;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.List;

public class DiscardOperation extends BaseContentManager {
    private Boolean canDiscard = false;
    private Boolean isCollection = false;
    private static final List<String> CONTENT_DISCARD_STATUS = Platform.config.hasPath("content.discard.status") ?
            Platform.config.getStringList("content.discard.status") : Arrays.asList("Draft", "FlagDraft");
    private static List<String> CONTENT_LIVE_STATUS = Arrays.asList("Live", "Unlisted", "Retired");

    /**
     * This API will allow to discard content
     *
     * @param contentId
     * @return
     */
    public Response discard(String contentId) throws Exception {
        //Check if ContentId is null
        validateEmptyOrNull(contentId, "Content Id", ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name());
        //Get node
        Node node = getNode(contentId, false);
        if (node == null) {
            throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                    "Content not found with id: " + contentId, contentId);
        }
        String mimeType = (String) node.getMetadata().get("mimeType");
        if (mimeType != null)
            isCollection = StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType);
        //Check if resource is present
        //Check if status -> If live, check if there is image which is in draft/flag draft else if draft delete
        String status = (String) node.getMetadata().get("status");
        canDiscard = checkIfValidDiscardRequest(contentId, status);
        Response response;
        //Check mimeType -> Collection and Resource handled differently
        if (isCollection && canDiscard) {
            response = discardCollection(contentId, status);
        } else if (canDiscard) {
            response = discardContent(contentId, status);
        } else {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_NOT_DRAFT.name(),
                    "No changes to discard since Content Image status isn't draft" + contentId, contentId);
        }
        return getResult(response, contentId);

    }

    /**
     * Checks if content is Live or Draft, if live checks if image is present
     *
     * @param contentId
     * @param status
     * @return
     * @throws Exception
     */
    private Boolean checkIfValidDiscardRequest(String contentId, String status) throws Exception {
        if (status != null)
            if (CONTENT_DISCARD_STATUS.contains(status)) {
                return true;
            } else if (CONTENT_LIVE_STATUS.contains(status)) {
                Node node = getNode(contentId, true);
                if (node == null)
                    return false;
                if (CONTENT_DISCARD_STATUS.contains(node.getMetadata().get("status")))
                    return true;
            }
        return false;
    }

    /**
     * Check if content id is blank or not
     *
     * @param contentValue
     * @param contentName
     * @param contentErrorCode
     * @throws Exception
     */

    private void validateEmptyOrNull(Object contentValue, String contentName, String contentErrorCode) throws Exception {
        if (RequestValidatorUtil.isEmptyOrNull(contentValue)) {
            throw new ClientException(contentErrorCode,
                    contentName + " can not be blank or null");
        }
    }

    /**
     * Get the node from neo4j
     *
     * @param contentId
     * @param imageRequired
     * @return
     */

    private Node getNode(String contentId, Boolean imageRequired) {
        if (imageRequired)
            contentId = contentId + ".img";
        TelemetryManager.log("Fetching the Data For Content Id: " + contentId);
        Response response = getDataNode(TAXONOMY_ID, contentId);
        if (!checkError(response)) {
            Node node = (Node) response.get(GraphDACParams.node.name());
            return node;
        }
        return null;
    }

    /**
     * Delete image node in neo4j if live else node and also delete hierarchy in cassandra
     *
     * @param contentId
     * @param status
     * @return
     */
    private Response discardCollection(String contentId, String status) {
        String id = contentId;
        if (CONTENT_LIVE_STATUS.contains(status))
            contentId = contentId + ".img";
        // delete image..
        Request request = getRequest(TAXONOMY_ID, GraphEngineManagers.NODE_MANAGER, "deleteDataNode");
        request.put(ContentWorkflowPipelineParams.node_id.name(), contentId);
        Response response = getResponse(request);
        if (StringUtils.equalsIgnoreCase(ResponseCode.OK.name(), response.getResponseCode().name())) {
            Response resp = deleteHierarchy(Arrays.asList(id + ".img"));
            return resp;
        }
        return response;
    }

    /**
     * Delete the neo4j node (If live delete the image in draft)
     *
     * @param contentId
     */
    private Response discardContent(String contentId, String status) {
        if (CONTENT_LIVE_STATUS.contains(status))
            contentId = contentId + ".img";
        // delete image..
        Request request = getRequest(TAXONOMY_ID, GraphEngineManagers.NODE_MANAGER, "deleteDataNode");
        request.put(ContentWorkflowPipelineParams.node_id.name(), contentId);
        Response response = getResponse(request);
        return response;
    }

    /**
     * Get the json response
     *
     * @param response
     * @param contentId
     * @return
     */
    private Response getResult(Response response, String contentId) {
        response.getResult().put("node_id", contentId);
        if (!StringUtils.equalsIgnoreCase(ResponseCode.OK.name(), response.getResponseCode().name()))
            response.getResult().put("message", "Draft Changes couldn't be discarded for content id : " + contentId + "as not found");
        else
            response.getResult().put("message", "Draft Changes for content id : " + contentId + "are discarded");
        return response;
    }
}
