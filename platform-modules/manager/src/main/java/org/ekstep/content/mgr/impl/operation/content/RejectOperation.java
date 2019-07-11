package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RejectOperation extends BaseContentManager {


    public Response rejectContent(String contentId, Map<String, Object> requestMap) {
        validateEmptyOrNullContentId(contentId);
        Response getResponse = validateAndGetNodeResponseForOperation(contentId);
        if (checkError(getResponse))
            return getResponse;
        Node node = isValidRejectContent(getResponse);
        if (null == node)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_NOT_IN_REVIEW.name(), "Content is not in review state for identifier: " + node.getIdentifier());
        populateNodeMetadata(requestMap, node);
        Response response = updateNode(node.getIdentifier(), CONTENT_OBJECT_TYPE, node);
        response.put("node_id", contentId);
        return response;
    }

    private Node isValidRejectContent(Response response) {
        Node node = (Node) response.get(GraphDACParams.node.name());
        String status = (String) node.getMetadata().get("status");
        if (StringUtils.isBlank(status))
            throw new ClientException(ContentErrorCodes.ERR_METADATA_ISSUE.name(), "Content metadata error, status is blank for identifier:" + node.getIdentifier());
        if (reviewStatus.contains(status))
            return node;
        if (finalStatus.contains(status)) {
            Response getImageResponse = validateAndGetNodeResponseForOperation(node.getIdentifier() + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
            if (checkError(getImageResponse))
                return null;
            Node imageNode = ((Node) getImageResponse.get(GraphDACParams.node.name()));
            if(reviewStatus.contains(imageNode.getMetadata().get("status")))
                return imageNode;
        }
        return null;
    }

    private void populateNodeMetadata(Map<String, Object> requestMap, Node node) {
        Map<String, Object> metadata = node.getMetadata();
        String status = (String) node.getMetadata().get("status");
        if (Arrays.asList("FlagReview").contains(status))
            metadata.put("status", "FlagDraft");
        else
            metadata.put("status", "Draft");
        if (MapUtils.isNotEmpty(requestMap)) {
            if (null != requestMap.get("rejectReasons")) {
                if(!(requestMap.get("rejectReasons") instanceof List))
                    throw new ClientException(ContentErrorCodes.ERR_INVALID_REQUEST_FORMAT.name(), "rejectReasons should be a List");
                metadata.put("rejectReasons", (List<String>)requestMap.get("rejectReasons"));
            }
            if (null != requestMap.get("rejectComment"))
                metadata.put("rejectComment", requestMap.get("rejectComment"));
        }
        metadata.put("publishChecklist", null);
        metadata.put("publishComment", null);
        node.setMetadata(metadata);
    }


}
