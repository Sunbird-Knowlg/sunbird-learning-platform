package org.ekstep.content.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
public class ContentManagerReleaseDialcodeImpl extends BaseContentManager {

    public Response releaseDialcodes(String contentId, String channelId) throws Exception {
        if(StringUtils.isBlank(channelId))
            throw new ClientException(ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.name(),
                    "Channel can not be blank.");

        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
                    "Content Id Can Not be blank.");

        Node node = getContentNode(TAXONOMY_ID, contentId, null);

        validateRequest(node, channelId);

        List<String> reservedDialcodes = getReservedDialCodes(node).
                orElseThrow(() -> new ClientException(ContentErrorCodes.ERR_NO_RESERVED_DIALCODES.name(),
                        "Error! No DIAL Codes are Reserved for content:: " + node.getIdentifier()));

        Set<String> assignedDialcodes = new HashSet<>();
        populateAllAssisgnedDialcodesRecursive(node, assignedDialcodes);

        List<String> releasedDialcodes = assignedDialcodes.isEmpty() ?
                reservedDialcodes
                : reservedDialcodes.stream().filter(dialcode -> !assignedDialcodes.contains(dialcode)).collect(toList());

        if (releasedDialcodes.isEmpty())
            throw new ClientException(ContentErrorCodes.ERR_ALL_DIALCODES_UTILIZED.name(), "Error! All Reserved DIAL Codes are Utilized.");

        List<String> leftReservedDialcodes = reservedDialcodes.stream().filter(dialcode -> !releasedDialcodes.contains(dialcode)).collect(toList());

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(ContentAPIParams.reservedDialcodes.name(), leftReservedDialcodes);



        Response response = updateAllContents(contentId, updateMap);
        if (checkError(response)) {
            return response;
        } else {
            response.put(ContentAPIParams.count.name(), releasedDialcodes.size());
            response.put(ContentAPIParams.releasedDialcodes.name(), releasedDialcodes);
            response.put(ContentAPIParams.node_id.name(), contentId);
            TelemetryManager.info("DIAL Codes released.", response.getResult());
            return response;
        }
    }

    private void validateRequest(Node node, String channelId) {
        Map<String, Object> metadata = node.getMetadata();
        if (!StringUtils.equalsIgnoreCase(ContentAPIParams.Content.name(), node.getObjectType()))
            throw new ClientException(ContentErrorCodes.ERR_NOT_A_CONTENT.name(), "Error! Not a Content.");
        validateContentForReservedDialcodes(metadata);
        validateChannel(node.getMetadata(), channelId);
        if (StringUtils.equalsIgnoreCase((String) metadata.get(ContentAPIParams.status.name()), ContentAPIParams.Retired.name()))
            throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(), "Error! Content not found with id: " + metadata.get("identifier"));

    }

    private void populateAllAssisgnedDialcodesRecursive(Node node, Set<String> assignedDialcodes) {
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());

        getDialcodes(node).ifPresent(dialcodes -> assignedDialcodes.addAll(Arrays.asList(dialcodes)));

        getChildren(node, definition).ifPresent(childrens ->
                childrens.
                        stream().
                        map(NodeDTO::getIdentifier).
                        forEach(childIdentifier -> {
                            Node childNode = getContentNode(TAXONOMY_ID, childIdentifier, null);
                            if (isNodeVisibilityParent(childNode))
                                populateAllAssisgnedDialcodesRecursive(childNode, assignedDialcodes);
                        }));

        Response response = getDataNode(TAXONOMY_ID, node.getIdentifier() + ".img");
        if (!checkError(response)) {
            Node childImageNode = (Node) response.get(GraphDACParams.node.name());
            populateAllAssisgnedDialcodesRecursive(childImageNode, assignedDialcodes);
        }

    }

}
