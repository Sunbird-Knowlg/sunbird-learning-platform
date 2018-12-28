package org.ekstep.content.mgr.impl.hierarchy;

import org.ekstep.common.dto.Response;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.contentstore.CollectionStore;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;

import java.util.Map;

public class SyncHierarchyManager extends DummyBaseContentManager {

    private CollectionStore collectionStore = new CollectionStore();

    public Response syncHierarchy(String identifier) {
        Response getResponse = getDataNode(TAXONOMY_ID, identifier);
        Node rootNode = (Node) getResponse.get(GraphDACParams.node.name());
        if (checkError(getResponse)) {
            return getResponse;
        } else {
            DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
            Map<String, Object> hierarchy = util.getContentHierarchyRecursive(rootNode.getGraphId(), rootNode, definition, null, true);
            this.collectionStore.updateContentHierarchy(identifier, hierarchy);
            return OK();
        }

    }

}
