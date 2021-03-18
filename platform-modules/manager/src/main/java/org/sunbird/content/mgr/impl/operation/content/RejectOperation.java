package org.sunbird.content.mgr.impl.operation.content;

import com.google.common.base.Throwables;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Rhea Fernandes
 */

public class RejectOperation extends BaseContentManager {

    public Response rejectContent(String contentId, Map<String, Object> requestMap) {
        requestMap = (requestMap == null) ? new HashMap<String, Object>() : requestMap;
        if (StringUtils.isBlank(contentId) || StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
            throw new ClientException(ContentErrorCodes.ERR_INVALID_CONTENT_ID.name(),
                    "Please provide valid content identifier");
        }
        Node node = isValidRejectContent(contentId);
        if (null == node)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_NOT_IN_REVIEW.name(), "Content is not in review state for identifier: " + contentId);
        Map<String, Object> map = populateNodeMetadata(requestMap, node);
        try {
            Node updatedNode = ConvertToGraphNode.convertToGraphNode(map, getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE), node);
            Response response = updateNode(node.getIdentifier(), CONTENT_OBJECT_TYPE, updatedNode);
            response.put("node_id", contentId);
            return response;
        } catch (Exception e) {
            TelemetryManager.error("Error occured while updating content node for identifier:" + contentId, e);
            throw Throwables.propagate(e);
        }
    }

    private Node isValidRejectContent(String contentId) {
        Node node = getContentNode(TAXONOMY_ID, contentId, "edit");
        String status = (String) node.getMetadata().get("status");
        if (StringUtils.isBlank(status))
            throw new ClientException(ContentErrorCodes.ERR_METADATA_ISSUE.name(), "Content metadata error, status is blank for identifier:" + node.getIdentifier());
        if (reviewStatus.contains(status))
            return node;
        return null;
    }

    private Map<String,Object> populateNodeMetadata(Map<String, Object> requestMap, Node node) {
        String status = (String) node.getMetadata().get("status");
        if (Arrays.asList("FlagReview").contains(status))
            requestMap.put("status", "FlagDraft");
        else
            requestMap.put("status", "Draft");
        requestMap.put("versionKey", node.getMetadata().get("versionKey"));
        if (MapUtils.isNotEmpty(requestMap) && null != requestMap.get("rejectReasons")
                &&  !(requestMap.get("rejectReasons") instanceof List) ) {
            throw new ClientException(ContentErrorCodes.ERR_INVALID_REQUEST_FORMAT.name(), "rejectReasons should be a List");
        }
        requestMap.put("publishChecklist", null);
        requestMap.put("publishComment", null);
        return requestMap;
    }

}
