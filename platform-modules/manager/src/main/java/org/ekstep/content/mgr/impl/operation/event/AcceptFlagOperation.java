package org.ekstep.content.mgr.impl.operation.event;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AcceptFlagOperation extends BaseContentManager {
    
    @SuppressWarnings("unchecked")
    public Response acceptFlag(String contentId) {
        Response response;
        boolean isImageNodeExist = false;
        String versionKey;

        validateEmptyOrNullContentId(contentId);

        Response getResponse = validateAndGetNodeResponseForOperation(contentId);

        Node node = (Node) getResponse.get(GraphDACParams.node.name());
        boolean isValidObj = VALID_FLAG_OBJECT_TYPES.contains(node.getObjectType());
        boolean isValidStatus = StringUtils.equalsIgnoreCase((String) node.getMetadata().get("status"), "Flagged");

        if (!isValidObj || !isValidStatus)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                    "Invalid Flagged Content! Content Can Not Be Accepted.");

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
        List<String> externalPropsList = getExternalPropsList(definition);

        String imageContentId = getImageId(contentId);
        Response imageResponse = getDataNode(TAXONOMY_ID, imageContentId);
        if (!checkError(imageResponse))
            isImageNodeExist = true;

        if (!isImageNodeExist) {
            Node createNode = (Node) getResponse.get(GraphDACParams.node.name());
            createNode.setIdentifier(imageContentId);
            createNode.setObjectType(node.getObjectType() + DEFAULT_OBJECT_TYPE_IMAGE_SUFFIX);
            createNode.getMetadata().put(ContentAPIParams.status.name(), "FlagDraft");
            createNode.setGraphId(TAXONOMY_ID);
            Response createResponse = createDataNode(createNode);
            if (!checkError(createResponse)) {
                TelemetryManager.log("Updating external props for: " + imageContentId);
                Response bodyResponse = getContentProperties(contentId, externalPropsList);
                if (!checkError(bodyResponse)) {
                    Map<String, Object> extValues = (Map<String, Object>) bodyResponse
                            .get(ContentStoreParams.values.name());
                    if (null != extValues && !extValues.isEmpty())
                        updateContentProperties(imageContentId, extValues);
                }
                versionKey = (String) createResponse.get("versionKey");
            } else
                return createResponse;
        } else {
            TelemetryManager.log("Updating Image node: " + imageContentId);
            Node imageNode = (Node) imageResponse.get(GraphDACParams.node.name());
            imageNode.setGraphId(TAXONOMY_ID);
            imageNode.getMetadata().put(ContentAPIParams.status.name(), "FlagDraft");
            Response updateResponse = updateDataNode(imageNode);
            if (checkError(updateResponse))
                return updateResponse;
            versionKey = (String) updateResponse.get("versionKey");
        }
        TelemetryManager.log("Updating Original node: " + contentId);
        getResponse = getDataNode(TAXONOMY_ID, contentId);
        Node originalNode = (Node) getResponse.get(GraphDACParams.node.name());
        originalNode.getMetadata().put(ContentAPIParams.status.name(), "Retired");
        Response retireResponse = updateDataNode(originalNode);
        clearRedisCache(contentId);
        if (!checkError(retireResponse)) {
            response = getSuccessResponse();
            response.getResult().put("node_id", contentId);
            response.getResult().put("versionKey", versionKey);
            return response;
        } else {
            return retireResponse;
        }
    }

}
