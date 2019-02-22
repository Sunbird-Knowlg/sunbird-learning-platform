package org.ekstep.content.mgr.impl.operation.dialcodes;

import org.apache.commons.collections.MapUtils;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;

public class ReleaseDialcodesOperation extends BaseContentManager {

    public Response releaseDialCodes(String contentId, String channelId) throws Exception {
        validateEmptyOrNullChannelId(channelId);
        validateEmptyOrNullContentId(contentId);

        Node node = getContentNode(TAXONOMY_ID, contentId, null);

        validateRequest(node, channelId);

        Map<String, Integer> reservedDialcodeMap = getReservedDialCodes(node);
        if(null == reservedDialcodeMap || MapUtils.isEmpty(reservedDialcodeMap))
                throw new ClientException(ContentErrorCodes.ERR_NO_RESERVED_DIALCODES.name(),
                        "Error! No DIAL Codes are Reserved for content:: " + node.getIdentifier());

        Set<String> assignedDialcodes = new HashSet<>();
        populateAllAssisgnedDialcodesRecursive(node, assignedDialcodes);

        List<String> reservedDialcodes = new ArrayList<>(reservedDialcodeMap.keySet());
        List<String> releasedDialcodes = assignedDialcodes.isEmpty() ? 
        		reservedDialcodes
                : reservedDialcodes.stream().filter(dialcode -> !assignedDialcodes.contains(dialcode)).collect(toList());

        if (releasedDialcodes.isEmpty())
            throw new ClientException(ContentErrorCodes.ERR_ALL_DIALCODES_UTILIZED.name(), "Error! All Reserved DIAL Codes are Utilized.");

        releasedDialcodes.stream().forEach(dialcode -> reservedDialcodeMap.remove(dialcode));
        Map<String, Object> updateMap = new HashMap<>();
        if(MapUtils.isEmpty(reservedDialcodeMap))
        		updateMap.put(ContentAPIParams.reservedDialcodes.name(), null);
        	else
        		updateMap.put(ContentAPIParams.reservedDialcodes.name(), reservedDialcodeMap);
        
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

        validateIsContent(node);
        validateContentForReservedDialcodes(metadata);
        validateChannel(metadata, channelId);
        validateIsNodeRetired(metadata);
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
