package org.sunbird.content.mgr.impl.operation.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.util.RequestValidatorUtil;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhea Fernandes
 */
public class DiscardOperation extends BaseContentManager {
    private Boolean isCollection = false;
    private static final List<String> CONTENT_DISCARD_STATUS = Platform.config.hasPath("content.discard.status") ?
            Platform.config.getStringList("content.discard.status") : Arrays.asList("Draft", "FlagDraft");
    private static final List<String> REMOVE_PUBLISHED_DATA = Arrays.asList("compatibilityLevel", "lastPublishedOn", "pkgVersion", "leafNodesCount", "downloadUrl", "variants");

    /**
     * This API will allow to discard content
     * @param contentId
     * @return
     */
    public Response discard(String contentId) throws Exception {
        Response response;
        if (StringUtils.isBlank(contentId) || StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
            throw new ClientException(ContentErrorCodes.ERR_INVALID_CONTENT_ID.name(),
                    "Please provide valid content identifier");
        }
        Node imageNode = getNode(contentId, true);
        if (imageNode != null) {
            String objectType = imageNode.getObjectType();
            if(StringUtils.equalsIgnoreCase(objectType, "ContentImage"))
                response = discardNode(imageNode);
            else
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_INVALID_CONTENT_ID.name(), "Content Id is invalid");
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
            throw new ClientException(ContentErrorCodes.ERR_METADATA_ISSUE.name(), "Content Status and/or Mimetype can't be blank for content id: " +contentId);
        }
        if (CONTENT_DISCARD_STATUS.contains(status)) {
            if (isCollection) {
                Response responseCollection = discardCollection(contentId);
                if (!checkError(responseCollection)) {
                    return discardContent(contentId);
                }
                return responseCollection;
            } else {
                return discardContent(contentId);
            }
        } else {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_NOT_DRAFT.name(),
                    "No changes to discard for content with content id: "+ contentId + " since content status isn't draft" , contentId);
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
        Response resp = null;
        if(StringUtils.endsWithIgnoreCase(contentId, ".img")){
            resp = updateImageHierarchy(contentId);
        }else {
            identifier = identifier + ".img";
            resp = deleteHierarchy(Arrays.asList(identifier));
        }
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
            response.getResult().put("message", "Draft version of the content with id: " + contentId + " is not found");
        else
            response.getResult().put("message", "Draft version of the content with id : " + contentId + " is discarded");
        return response;
    }

    private Response updateImageHierarchy(String contentId) {
        Response response = getCollectionHierarchy(contentId.replace(".img", ""));
        if (!checkError(response)) {
            Map<String, Object> liveHierarchy = (Map<String, Object>) response.getResult().get("hierarchy");
            List<Map<String, Object>> children = (List<Map<String, Object>>) liveHierarchy.get("children");
            Node node = getNode(contentId.replace(".img", ""), false);
            updateHierarchyMetadata(children, node);
            Map<String,Object> hierarchy = new HashMap<>();
            hierarchy.put("identifier", node.getIdentifier());
            hierarchy.put("children", children);
            Response resp = updateCollectionHierarchy(node.getIdentifier()+".img", hierarchy);
            return resp;
        }
        return response;
    }

    private void updateHierarchyMetadata(List<Map<String, Object>> children, Node node) {
        if (CollectionUtils.isNotEmpty(children)) {
            for (Map<String, Object> child : children) {
                if (StringUtils.equalsIgnoreCase("Parent",
                        (String) child.get("visibility"))) {
                    populatePublishMetadata(child, node);
                    updateHierarchyMetadata((List<Map<String, Object>>) child.get("children"), node);
                }
            }
        }
    }

    private void populatePublishMetadata(Map<String, Object> content, Node node) {
        REMOVE_PUBLISHED_DATA.forEach(property -> content.remove(property));
        content.put("status", "Draft");
        content.put("lastUpdatedOn", (String) node.getMetadata().get("lastUpdatedOn"));
    }
}

