package org.sunbird.content.mgr.impl.operation.event;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.publish.PublishManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.Map;

public class PublishOperation extends BaseContentManager {

    private PublishManager publishManager = new PublishManager();

    public Response publish(String contentId, Map<String, Object> requestMap) {

        validateEmptyOrNullContentId(contentId);

        Response response;

        Node node = getNodeForOperation(contentId, "publish");
        isNodeUnderProcessing(node, "Publish");

        String publisher = null;
        if (null != requestMap && !requestMap.isEmpty()) {
            if (!validateList(requestMap.get("publishChecklist"))) {
                requestMap.put("publishChecklist", null);
            }
            publisher = (String) requestMap.get("lastPublishedBy");
            node.getMetadata().putAll(requestMap);

            node.getMetadata().put("rejectReasons", null);
            node.getMetadata().put("rejectComment", null);
        }
        if (StringUtils.isNotBlank(publisher)) {
            TelemetryManager.log("LastPublishedBy: " + publisher);
            node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), publisher);
        } else {
            node.getMetadata().put("lastPublishedBy", null);
            node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), null);
        }

        try {
            response = publishManager.publish(contentId, node);
        } catch (ClientException e) {
            throw e;
        } catch (ServerException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
                    "Error occured during content publish");
        }

        TelemetryManager.log("Returning 'Response' Object.");
        if (StringUtils.endsWith(response.getResult().get("node_id").toString(), ".img")) {
            String identifier = (String) response.getResult().get("node_id");
            String new_identifier = identifier.replace(".img", "");
            TelemetryManager.log("replacing image id with content id in response" + identifier + new_identifier);
            response.getResult().replace("node_id", identifier, new_identifier);
        }
        return response;
    }

}
