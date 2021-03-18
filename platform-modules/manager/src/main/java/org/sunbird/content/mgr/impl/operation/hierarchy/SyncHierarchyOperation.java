package org.sunbird.content.mgr.impl.operation.hierarchy;

import org.sunbird.common.dto.Response;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.hierarchy.store.HierarchyStore;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;

import java.util.Map;

public class SyncHierarchyOperation extends BaseContentManager {

    private HierarchyStore hierarchyStore = new HierarchyStore();

    public Response syncHierarchy(String identifier) {
        Response getResponse = getDataNode(TAXONOMY_ID, identifier);
        Node rootNode = (Node) getResponse.get(GraphDACParams.node.name());
        if (checkError(getResponse)) {
            return getResponse;
        } else {
            DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
            Map<String, Object> hierarchy = util.getHierarchyMap(rootNode.getGraphId(), rootNode.getIdentifier(), definition, null,
                    null);
            this.hierarchyStore.saveOrUpdateHierarchy(identifier, hierarchy);
            return OK();
        }

    }

}
