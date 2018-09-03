package org.ekstep.sync.tool.mgr;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.contentstore.CollectionStore;
import org.ekstep.learning.util.ControllerUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component("hierarchySyncManager")
public class HierarchySyncManager {

    private ControllerUtil util = new ControllerUtil();

    private CollectionStore collectionStore = new CollectionStore();

    private int batchSize = Platform.config.hasPath("batch.size") ? Platform.config.getInt("batch.size"): 50;

    public void syncHierarchy(String graphId) {
        if (StringUtils.isBlank(graphId))
            throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");

        List<String> identifiers = util.getPublishedCollections(graphId);
        DefinitionDTO definition = util.getDefinition(graphId, "Content");

        if(CollectionUtils.isEmpty(identifiers)) {
            System.out.println("No objects found in this graph.");
        }else {
            System.out.println("No.of collections to be synced : " + identifiers.size());

            System.out.println("-----------------------------------------");

            syncCollections(graphId, identifiers, definition);

        }
    }

    private void syncCollections(String graphId, List<String> identifiers, DefinitionDTO definition) {
        long startTime = System.currentTimeMillis();
        long status = 0;
        for(List<String> ids : Lists.partition(identifiers, batchSize)) {
            Response response = util.getDataNodes(graphId, ids);

            if (response.getResponseCode() != ResponseCode.OK)
                throw new ResourceNotFoundException("ERR_SYNC_OBJECT_NOT_FOUND",
                        "Error: " + response.getParams().getErrmsg());

            List<Node> nodes = (List<Node>) response.getResult().get(GraphDACParams.node_list.name());
            if(CollectionUtils.isEmpty(nodes)){
                throw new ResourceNotFoundException("ERR_SYNC_OBJECT_NOT_FOUND", "Objects not found ");
            }


            for(Node node: nodes) {
                Map<String, Object> hierarchy = util.getContentHierarchyRecursive(node.getGraphId(), node, definition, null, true);
                collectionStore.updateContentHierarchy(node.getIdentifier(), hierarchy);

            }
            status += ids.size();
            System.out.println("Completed " + status + " collections out of " + identifiers.size());
        }
        System.out.println("-----------------------------------------");
        long endTime = System.currentTimeMillis();
        System.out.println("Sync completed at " + endTime);
        System.out.println("Time taken to sync nodes: " + (endTime - startTime) + "ms");
    }
}
