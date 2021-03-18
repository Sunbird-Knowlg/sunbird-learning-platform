package org.sunbird.content.mgr.impl.operation.dialcodes;


import org.codehaus.jackson.type.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.util.RequestValidatorUtil;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.learning.hierarchy.store.HierarchyStore;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;

public class ReleaseDialcodesOperation extends BaseContentManager {
    private HierarchyStore hierarchyStore = new HierarchyStore();
    private final ObjectMapper mapper = new ObjectMapper();

    public ReleaseDialcodesOperation() { }

    public ReleaseDialcodesOperation(HierarchyStore hierarchyStore) {
        this.hierarchyStore = hierarchyStore;
    }

    public Response releaseDialCodes(String contentId, String channelId) throws Exception {
        validateEmptyOrNull(channelId, "Channel", ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.name());
        validateEmptyOrNull(contentId, "Content Object Id", ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name());

        Node node = getContentNode(TAXONOMY_ID, contentId, null);

        validateRequest(node, channelId);

        Map<String, Integer> reservedDialcodeMap = getReservedDialCodes(node);
        if (MapUtils.isEmpty(reservedDialcodeMap))
            throw new ClientException(ContentErrorCodes.ERR_NO_RESERVED_DIALCODES.name(),
                    "Error! No DIAL Codes are Reserved for content: " + node.getIdentifier());

        Boolean isLive = false;
        Set assignedDialCodes = new HashSet();
        Set releasedDialcodes = new HashSet();

        assignedDialCodes.addAll(getDialCodes(node.getMetadata(), "Parent"));

        Map<String, Object> imageHierarchy = hierarchyStore.getHierarchy(contentId + ".img");
        if (MapUtils.isEmpty(imageHierarchy)) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), "Hierarchy is null for :" + contentId);
        }

        populateAssignedDialCodes(imageHierarchy,assignedDialCodes);

        isLive = (StringUtils.equalsIgnoreCase("Live", (String) node.getMetadata().get("status")) ||
                  StringUtils.equalsIgnoreCase("Unlisted", (String) node.getMetadata().get("status"))) ? true : isLive;

        if (isLive) {
            Response response = getDataNode(TAXONOMY_ID, contentId + ".img");
            if (!checkError(response)) {
                Node imageNode = (Node) response.get(GraphDACParams.node.name());
                assignedDialCodes.addAll(getDialCodes(imageNode.getMetadata(), "Parent"));

            }
            Map<String, Object> liveHierarchy = hierarchyStore.getHierarchy(contentId);
            if (MapUtils.isEmpty(liveHierarchy)) {
                throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), "Hierarchy is null for :" + contentId);
            }
            populateAssignedDialCodes(liveHierarchy,assignedDialCodes);
        }

        releasedDialcodes.addAll(getReleasedDialcodes(reservedDialcodeMap,assignedDialCodes));

        if (releasedDialcodes.isEmpty())
            throw new ClientException(ContentErrorCodes.ERR_ALL_DIALCODES_UTILIZED.name(), "Error! All Reserved DIAL Codes are Utilized.");

        releasedDialcodes.stream().forEach(dialcode -> reservedDialcodeMap.remove(dialcode));

        Map<String, Object> updateMap = new HashMap<>();

        if (MapUtils.isEmpty(reservedDialcodeMap))
            updateMap.put(ContentAPIParams.reservedDialcodes.name(), null);
        else
            updateMap.put(ContentAPIParams.reservedDialcodes.name(), reservedDialcodeMap);
        return getResponse(contentId, updateMap, releasedDialcodes);
    }

    private Response getResponse(String contentId, Map<String, Object> updateMap, Set releasedDialcodes) throws Exception {
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

    private void validateEmptyOrNull(Object contentValue, String contentName, String contentErrorCode) {
        if (RequestValidatorUtil.isEmptyOrNull(contentValue)) {
            throw new ClientException(contentErrorCode,
                    contentName + " can not be blank or null");
        }
    }

    private void validateRequest(Node node, String channelId) {
        Map<String, Object> metadata = node.getMetadata();

        validateIsContent(node);
        validateContentForReservedDialcodes(metadata);
        validateChannel(metadata, channelId);
        validateIsNodeRetired(metadata);
    }
    public void populateAssignedDialCodes(Map<String, Object> hierarchyMap, Set assignedDialCodes) {
        if (hierarchyMap != null) {
            List<Map<String, Object>> childrenMaps = mapper.convertValue(hierarchyMap.get("children"), new TypeReference<List<Map<String, Object>>>() {
            });
            getAssignedDialCodes(childrenMaps, assignedDialCodes);
        }
    }

    public List<String> getReleasedDialcodes( Map<String, Integer> reservedDialcodeMap, Set assignedDialCodes) {
        List<String> releasedDialCodes;
        List<String> reservedDialCodes = new ArrayList<>();
        reservedDialCodes.addAll(reservedDialcodeMap.keySet());
        releasedDialCodes = assignedDialCodes.isEmpty() ?
                reservedDialCodes
                : reservedDialCodes.stream().filter(dialcode -> !assignedDialCodes.contains(dialcode)).collect(toList());
        return releasedDialCodes;
    }

    private void getAssignedDialCodes(List<Map<String, Object>> children, Set<String> assignedDialcodes) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                assignedDialcodes.addAll(getDialCodes(child, "Default"));
                if (child.containsKey("children") && !child.get("visibility").toString().equals("Default")) {
                    getAssignedDialCodes((List<Map<String, Object>>) child.get("children"), assignedDialcodes);
                }
            });
        }
    }

    public List<String> getDialCodes(Map<String, Object> childMap, String visibility) {
        if (childMap.containsKey("dialcodes") && childMap.containsKey("visibility") && !childMap.get("visibility").toString().equals(visibility)) {
            List<String> dialcodes = mapper.convertValue(childMap.get("dialcodes"), new TypeReference<List<String>>() {});
            return (dialcodes.stream().filter(f -> StringUtils.isNotBlank(f)).collect(toList()));
        }
        return new ArrayList<>();
    }
}
