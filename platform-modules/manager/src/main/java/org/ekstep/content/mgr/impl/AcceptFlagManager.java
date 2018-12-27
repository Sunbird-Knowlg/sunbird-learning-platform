package org.ekstep.content.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class AcceptFlagManager extends DummyBaseContentManager {

    @SuppressWarnings("unchecked")
    public Response acceptFlag(String contentId) {
        Response response = new Response();
        boolean isImageNodeExist = false;
        String versionKey = null;

        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                    "Content Id Can Not be blank.");
        Response getResponse = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(getResponse))
            throw new ResourceNotFoundException(TaxonomyErrorCodes.ERR_NODE_NOT_FOUND.name(),
                    "Content Not Found With Identifier: " + contentId);

        Node node = (Node) getResponse.get(GraphDACParams.node.name());
        boolean isValidObj = StringUtils.equalsIgnoreCase(CONTENT_OBJECT_TYPE, node.getObjectType());
        boolean isValidStatus = StringUtils.equalsIgnoreCase((String) node.getMetadata().get("status"), "Flagged");

        if (!isValidObj || !isValidStatus)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                    "Invalid Flagged Content! Content Can Not Be Accepted.");

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        List<String> externalPropsList = getExternalPropsList(definition);

        String imageContentId = getImageId(contentId);
        Response imageResponse = getDataNode(TAXONOMY_ID, imageContentId);
        if (!checkError(imageResponse))
            isImageNodeExist = true;

        if (!isImageNodeExist) {
            Node createNode = (Node) getResponse.get(GraphDACParams.node.name());
            createNode.setIdentifier(imageContentId);
            createNode.setObjectType(CONTENT_IMAGE_OBJECT_TYPE);
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
        if (!checkError(retireResponse)) {
            if (StringUtils.equalsIgnoreCase((String) originalNode.getMetadata().get("mimeType"),
                    "application/vnd.ekstep.content-collection"))
                deleteHierarchy(Arrays.asList(contentId));
            response = getSuccessResponse();
            response.getResult().put("node_id", contentId);
            response.getResult().put("versionKey", versionKey);
            return response;
        } else {
            return retireResponse;
        }
    }

}
