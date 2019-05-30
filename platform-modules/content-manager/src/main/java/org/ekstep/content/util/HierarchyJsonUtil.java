package org.ekstep.content.util;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface HierarchyJsonUtil {
    public final String TAXONOMY_ID = "domain";
    public final ControllerUtil util = new ControllerUtil();
    public final ObjectMapper mapper = new ObjectMapper();


    public static File createHierarchyFile(String bundlePath,
                                           Node node, List<Map<String, Object>> children) throws Exception {
        String contentId = node.getIdentifier();
        File hierarchyFile = null;
        if (node == null || StringUtils.isBlank(bundlePath)) {
            TelemetryManager.error("Hierarchy File creation failed for identifier : " +contentId);
            return hierarchyFile;
        }
        Map<String, Object> hierarchyMap = getContentMap(node, children);
        if (MapUtils.isNotEmpty(hierarchyMap)) {
            hierarchyFile = new File(bundlePath + File.separator + ContentConfigurationConstants.CONTENT_BUNDLE_HIERARCHY_FILE_NAME);
            if (hierarchyFile == null) {
                TelemetryManager.error("Hierarchy File creation failed for identifier : " + contentId);
                return hierarchyFile;
            }
            String hierarchyJSON = mapper.writeValueAsString(hierarchyMap);
            FileUtils.writeStringToFile(hierarchyFile, hierarchyJSON);
            TelemetryManager.log("Hierarchy JSON Written for identifier : " +contentId);
        } else {
            TelemetryManager.log("Hierarchy JSON can't be created for identifier : " +contentId);
        }
        return hierarchyFile;
    }

    public static Map<String, Object> getContentMap(Node node, List<Map<String, Object>> childrenList) {
        DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, "Content");
        Map<String, Object> collectionHierarchy = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
        collectionHierarchy.put("children", childrenList);
        collectionHierarchy.put("identifier", node.getIdentifier());
        collectionHierarchy.put("objectType", node.getObjectType());
        return collectionHierarchy;
    }
}
