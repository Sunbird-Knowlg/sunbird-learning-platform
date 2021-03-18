package org.sunbird.content.mgr.impl.operation.content.update;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.contentstore.ContentStoreParams;
import org.sunbird.taxonomy.enums.TaxonomyAPIParams;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateContentOperation extends BaseContentManager {

    @SuppressWarnings("unchecked")
    public Response update(String contentId, Map<String, Object> map) throws Exception {
        if (null == map)
            return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

        if (map.containsKey("dialcodes")) {
            map.remove("dialcodes");
        }
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        restrictProps(definition, map, "status", "framework");

        String originalId = contentId;
        String objectType = CONTENT_OBJECT_TYPE;
        map.put("objectType", CONTENT_OBJECT_TYPE);
        map.put("identifier", contentId);

        String mimeType = (String) map.get(TaxonomyAPIParams.mimeType.name());
        updateDefaultValuesByMimeType(map, mimeType);

        boolean isImageObjectCreationNeeded = false;
        boolean imageObjectExists = false;

        String contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
        Response getNodeResponse = getDataNode(TAXONOMY_ID, contentImageId);
        if (checkError(getNodeResponse)) {
            TelemetryManager.log("Content image not found: " + contentImageId);
            isImageObjectCreationNeeded = true;
            getNodeResponse = getDataNode(TAXONOMY_ID, contentId);
            TelemetryManager.log("Content node response: " + getNodeResponse);
        } else
            imageObjectExists = true;

        if (checkError(getNodeResponse)) {
            TelemetryManager.log("Content not found: " + contentId);
            return getNodeResponse;
        }

        if (map.containsKey(ContentAPIParams.body.name()))
            map.put(ContentAPIParams.artifactUrl.name(), null);

        Map<String, Object> externalProps = new HashMap<String, Object>();
        List<String> externalPropsList = getExternalPropsList(definition);
        if (null != externalPropsList && !externalPropsList.isEmpty()) {
            for (String prop : externalPropsList) {
                if (null != map.get(prop))
                    externalProps.put(prop, map.get(prop));
                if (StringUtils.equalsIgnoreCase(ContentAPIParams.screenshots.name(), prop) && null != map.get(prop)) {
                    map.put(prop, null);
                } else {
                    map.remove(prop);
                }

            }
        }

        Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
        TelemetryManager.log("Graph node found: " + graphNode.getIdentifier());
        Map<String, Object> metadata = graphNode.getMetadata();
        String status = (String) metadata.get("status");
        String inputStatus = (String) map.get("status");
        if (null != inputStatus) {
            if (reviewStatus.contains(inputStatus) && !reviewStatus.contains(status)) {
                map.put("lastSubmittedOn", DateUtils.format(new Date()));
            }
        }

        boolean checkError = false;
        Response createResponse = null;
        if (finalStatus.contains(status)) {
            if (isImageObjectCreationNeeded) {
                graphNode.setIdentifier(contentImageId);
                graphNode.setObjectType(CONTENT_IMAGE_OBJECT_TYPE);
                metadata.put("status", "Draft");
                Object lastUpdatedBy = map.get("lastUpdatedBy");
                if (null != lastUpdatedBy)
                    metadata.put("lastUpdatedBy", lastUpdatedBy);
                graphNode.setGraphId(TAXONOMY_ID);
                createResponse = createImageNode(graphNode, (String) metadata.get("channel"));
                checkError = checkError(createResponse);
                if (!checkError) {
                    TelemetryManager.log("Updating external props for: " + contentImageId);
                    Response bodyResponse = getContentProperties(contentId, externalPropsList);
                    checkError = checkError(bodyResponse);
                    if (!checkError) {
                        Map<String, Object> extValues = (Map<String, Object>) bodyResponse
                                .get(ContentStoreParams.values.name());
                        if (null != extValues && !extValues.isEmpty()) {
                            updateContentProperties(contentImageId, extValues);
                        }
                    }
                    map.put("versionKey", createResponse.get("versionKey"));
                }
            }
            objectType = CONTENT_IMAGE_OBJECT_TYPE;
            contentId = contentImageId;
        } else if (imageObjectExists) {
            objectType = CONTENT_IMAGE_OBJECT_TYPE;
            contentId = contentImageId;
        }

        if (checkError)
            return createResponse;

        TelemetryManager.log("Updating content node: " + contentId);
        Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
        domainObj.setGraphId(TAXONOMY_ID);
        domainObj.setIdentifier(contentId);
        domainObj.setObjectType(objectType);
        createResponse = updateDataNode(domainObj);
        checkError = checkError(createResponse);
        if (checkError)
            return createResponse;

        createResponse.put(GraphDACParams.node_id.name(), originalId);

        if (null != externalProps && !externalProps.isEmpty()) {
            Response externalPropsResponse = updateContentProperties(contentId, externalProps);
            if (checkError(externalPropsResponse))
                return externalPropsResponse;
        }
        return createResponse;
    }

}
