package org.ekstep.content.mgr.impl.operation.upload;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.content.UpdateOperation;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.util.MimeTypeManagerFactory;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.HashMap;
import java.util.Map;

public class BaseUploadOperation extends BaseContentManager {

    private final UpdateOperation updateOperation = new UpdateOperation();

    protected IMimeTypeManager getMimeTypeManger(String contentId, String mimeType, Node node) {
        TelemetryManager.log(
                "Fetching Mime-Type Factory For Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
        String contentType = getContentTypeFrom(node);/*(String) node.getMetadata().get("contentType");*/
        return MimeTypeManagerFactory.getManager(contentType, mimeType);
    }

    public Response updateMimeType(String contentId, String mimeType) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("mimeType", mimeType);
        map.put("versionKey", Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY));
        return this.updateOperation.update(contentId, map);
    }

    protected void setMimeTypeForUpload(String mimeType, Node node) {
        node.getMetadata().put("mimeType", mimeType);
        updateDefaultValuesByMimeType(node.getMetadata(), mimeType);
    }

    protected Response validateResponseAndUpdateMimeType(boolean updateMimeType, Node node, Response res, String contentId, String mimeType) throws Exception {
        if (updateMimeType && !checkError(res)) {
            node.getMetadata().put("versionKey", res.getResult().get("versionKey"));
            return updateMimeType(contentId, mimeType);
        }
        return null;
    }

    protected Response checkAndReturnUploadResponse(Response res) {
        if (checkError(res)) {
            return res;
        } else {
            String nodeId = (String) res.getResult().get("node_id");
            String returnNodeId = getId(nodeId);
            res.getResult().replace("node_id", nodeId, returnNodeId);
            return res;
        }
    }

}
