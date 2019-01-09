package org.ekstep.content.mgr.impl.operation.hierarchy;

import org.ekstep.common.dto.Response;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SyncHierarchyOperation extends BaseContentManager {

    private HierarchyStore hierarchyStore = new HierarchyStore();

    public Response syncHierarchy(String identifier) {
        Response getResponse = getDataNode(TAXONOMY_ID, identifier);
        Node rootNode = (Node) getResponse.get(GraphDACParams.node.name());
        if (checkError(getResponse)) {
            return getResponse;
        } else {
            DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
            Map<String, Object> hierarchy = util.getContentHierarchyRecursive(rootNode.getGraphId(), rootNode, definition, null, true);
            this.hierarchyStore.saveOrUpdateHierarchy(identifier, hierarchy);
            return OK();
        }

    }

}
