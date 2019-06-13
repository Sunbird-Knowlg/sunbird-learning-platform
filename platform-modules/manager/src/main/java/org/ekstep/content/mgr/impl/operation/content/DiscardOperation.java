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

/**
 * @author Rhea Fernandes
 */
public class DiscardOperation extends BaseContentManager {
    private Boolean isCollection = false;
    private static final List<String> CONTENT_DISCARD_STATUS = Platform.config.hasPath("content.discard.status") ?
            Platform.config.getStringList("content.discard.status") : Arrays.asList("Draft", "FlagDraft");
    /**
     * This API will allow to discard content
     * @param contentId
     * @return
     */
    public Response discard(String contentId) throws Exception {
        Response response;
        validateEmptyOrNull(contentId, "Content Id", ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name());
        Node imageNode = getNode(contentId, true);
        if (imageNode != null) {
            String objectType = imageNode.getObjectType();
            if(StringUtils.equalsIgnoreCase(objectType, "ContentImage"))
                response = discardNode(imageNode);
            else
                throw new ClientException(ContentErrorCodes.ERR_OBJECT_TYPE_MISMATCH.name(), "Content Image Node objectType isn't matching");
        } else {
            Node node = getNode(contentId, false);
            if (node == null) {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                        "Content not found with id: " + contentId, contentId);
            }
            response = discardNode(node);
        }
        return getResult(response, contentId);
    }

    /**
     * Validates if the content can be discarded
     * @param node
     * @return
     * @throws Exception
     */
    private Response discardNode(Node node) throws Exception {
        String contentId = node.getIdentifier();
        String mimeType = (String) node.getMetadata().get("mimeType");
        String status = (String) node.getMetadata().get("status");
        if (StringUtils.isNotBlank(mimeType) && StringUtils.isNotBlank(status)){
            isCollection = StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType);
        }else {
            throw new ClientException(ContentErrorCodes.ERR_METADATA_ISSUE.name(), "Content Status and/or Mimetype can't be null");
        }
        if (CONTENT_DISCARD_STATUS.contains(status)) {
            if (isCollection) {
                Response responseCollection = discardCollection(contentId);
                if (StringUtils.equalsIgnoreCase(ResponseCode.OK.name(), responseCollection.getResponseCode().name())) {
                    return discardContent(contentId);
                }
                return responseCollection;
            } else {
                return discardContent(contentId);

            }
        } else {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_NOT_DRAFT.name(),
                    "No changes to discard since content status isn't draft " + contentId, contentId);
        }
    }

    /**
     * Check if content id is blank or not
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
     * @param contentId
     * @param imageRequired
     * @return
     */

    private Node getNode(String contentId, Boolean imageRequired) {
        String identifier = contentId;
        if (imageRequired)
            identifier = identifier + ".img";
        TelemetryManager.log("Fetching the Data For Content Id: " + identifier);
        Response response = getDataNode(TAXONOMY_ID, identifier);
        if (!checkError(response)) {
            Node node = (Node) response.get(GraphDACParams.node.name());
            return node;
        }
        return null;
    }

    /**
     * Delete image node in neo4j if live else node and also delete hierarchy in cassandra
     * @param contentId
     * @return
     */
    private Response discardCollection(String contentId) {
        String identifier = contentId;
            if(!StringUtils.endsWithIgnoreCase(contentId, ".img"))
                identifier = identifier + ".img";
            Response resp = deleteHierarchy(Arrays.asList(identifier));
            return resp;
    }

    /**
     * Delete the neo4j node (If live delete the image in draft)
     * @param contentId
     */
    private Response discardContent(String contentId) {
        Request request = getRequest(TAXONOMY_ID, GraphEngineManagers.NODE_MANAGER, "deleteDataNode");
        request.put(ContentWorkflowPipelineParams.node_id.name(), contentId);
        Response response = getResponse(request);
        return response;
    }

    /**
     * Get the json response
     * @param response
     * @param contentId
     * @return
     */
    private Response getResult(Response response, String contentId) {
        response.getResult().put("node_id", contentId);
        if (!StringUtils.equalsIgnoreCase(ResponseCode.OK.name(), response.getResponseCode().name()))
            response.getResult().put("message", "Draft version of the content with id: " + contentId + "is not found");
        else
            response.getResult().put("message", "Draft version of the content with id : " + contentId + "is discarded");
        return response;
    }
}

