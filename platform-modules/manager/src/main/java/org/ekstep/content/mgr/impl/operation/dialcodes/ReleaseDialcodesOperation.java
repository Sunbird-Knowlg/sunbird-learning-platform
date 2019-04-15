package org.ekstep.content.mgr.impl.operation.dialcodes;


import org.codehaus.jackson.type.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.util.RequestValidatorUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;

public class ReleaseDialcodesOperation extends  BaseContentManager {
    private HierarchyStore hierarchyStore = new HierarchyStore();
    private final ObjectMapper mapper = new ObjectMapper();

    public ReleaseDialcodesOperation() {

    }
    public ReleaseDialcodesOperation(HierarchyStore hierarchyStore) {
        this.hierarchyStore = hierarchyStore;
    }


    public Response releaseDialCodes(String contentId, String channelId) throws Exception {
        validateEmptyOrNull(channelId, "Channel", ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.name());
        validateEmptyOrNull(channelId, "Content Object Id", ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name());
        Node node = getContentNode(TAXONOMY_ID, contentId, null);
        validateRequest(node, channelId);

        Map<String, Integer> reservedDialcodeMap = getReservedDialCodes(node);
        if(null == reservedDialcodeMap || MapUtils.isEmpty(reservedDialcodeMap))
                throw new ClientException(ContentErrorCodes.ERR_NO_RESERVED_DIALCODES.name(),
                        "Error! No DIAL Codes are Reserved for content:: " + node.getIdentifier());

        Map<String,Object> hierarchyMap = hierarchyStore.getHierarchy(contentId+ ".img");
        if(hierarchyMap != null){
            List<Map<String,Object>> childrenMaps = mapper.convertValue(hierarchyMap.get("children"),new TypeReference<List<Map<String,Object>>>(){});
            Set assignedDialCodes = new HashSet();
            populateAssignedDialCodes(childrenMaps,assignedDialCodes);
            List<String> reservedDialcodes = new ArrayList<>(reservedDialcodeMap.keySet());
            List<String> releasedDialcodes = assignedDialCodes.isEmpty() ?
                    reservedDialcodes
                    : reservedDialcodes.stream().filter(dialcode -> !assignedDialCodes.contains(dialcode)).collect(toList());

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
        }else {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), "Hierarchy is null for :"+contentId+".img");
        }
    }

    private void validateEmptyOrNull(Object contentValue, String contentName, String contentErrorCode) {
        if(RequestValidatorUtil.isEmptyOrNull(contentValue)) {
            throw new ClientException(contentErrorCode,
                    contentName +" can not be blank or null");
        }
    }

    private void validateRequest(Node node, String channelId) {
        Map<String, Object> metadata = node.getMetadata();

        validateIsContent(node);
        validateContentForReservedDialcodes(metadata);
        validateChannel(metadata, channelId);
        validateIsNodeRetired(metadata);
    }

    private  void populateAssignedDialCodes(List<Map<String, Object>> children, Set<String> assignedDialcodes) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child ->{
                assignedDialcodes.addAll(getDialCodes(child));
                if(child.containsKey("children") && !child.get("visibility").toString().equals("Default")){
                    populateAssignedDialCodes((List<Map<String,Object>>)child.get("children"),assignedDialcodes);
                } else {
                    assignedDialcodes.addAll(getDialCodes (child));
                }
            });
        }
    }

    public List<String> getDialCodes(Map<String,Object> childMap) {
        if(childMap.containsKey("dialcodes") && !childMap.get("visibility").toString().equals("Default")) {
            return ((List<String>) childMap.get("dialcodes")).stream().filter(f -> StringUtils.isNotBlank(f)).collect(toList());
        }
        return new ArrayList<>();
    }

}
