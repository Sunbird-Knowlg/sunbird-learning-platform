package org.ekstep.content.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.publish.PublishManager;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PublishMgr extends DummyBaseContentManager {

    private PublishManager publishManager = new PublishManager();

    public Response publish(String contentId, Map<String, Object> requestMap) {

        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

        Response response = new Response();

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
