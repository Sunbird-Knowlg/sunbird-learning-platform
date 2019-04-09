package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class RetireOperation extends BaseContentManager {

    /**
     * @param contentId
     * @return
     */
    public Response retire(String contentId) {
        Boolean isImageNodeExist = false;
        Map<String, Object> params = new HashMap<>();
        params.put("status", "Retired");
        params.put("lastStatusChangedOn", DateUtils.formatCurrentDate());

        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                    "Content Object Id cannot is Blank.");

        Response response = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(response))
            return response;

        Node node = (Node) response.get(GraphDACParams.node.name());
        String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
        String status = (String) node.getMetadata().get(ContentAPIParams.status.name());

        if (StringUtils.equalsIgnoreCase(ContentAPIParams.Retired.name(), status)) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_RETIRE.name(),
                    "Content with Identifier [" + contentId + "] is already Retired.");
        }

        Response imageNodeResponse = getDataNode(TAXONOMY_ID, getImageId(contentId));
        if (!checkError(imageNodeResponse))
            isImageNodeExist = true;

        List<String> identifiers = (isImageNodeExist) ? Arrays.asList(contentId, contentId + ".img") : Arrays.asList(contentId);

        response = updateDataNodes(params, identifiers, TAXONOMY_ID);
        if (checkError(response)) {
            return response;
        } else {
            if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType)) {
                //TODO: generate transcation event to sync es for units.
                deleteHierarchy(Arrays.asList(contentId));
            }
            Response responseNode = validateAndGetNodeResponseForOperation(contentId);
            node = (Node) responseNode.get("node");
            Response res = getSuccessResponse();
            res.put(ContentAPIParams.node_id.name(), node.getIdentifier());
            res.put(ContentAPIParams.versionKey.name(), node.getMetadata().get("versionKey"));
            return res;
        }
    }

    /**
     * @param map
     * @param idList
     * @param graphId
     * @return
     */
    private Response updateDataNodes(Map<String, Object> map, List<String> idList, String graphId) {
        Response response;
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
