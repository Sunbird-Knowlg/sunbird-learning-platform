package org.sunbird.sync.tool.mgr;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.hierarchy.store.HierarchyStore;
import org.sunbird.learning.util.ControllerUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component("hierarchySyncManager")
public class HierarchySyncManager {

    private ControllerUtil util = new ControllerUtil();

    private HierarchyStore hierarchyStore = new HierarchyStore();

    private int batchSize = Platform.config.hasPath("batch.size") ? Platform.config.getInt("batch.size"): 50;

    public void syncHierarchy(String graphId, String offset, String limit, String[] ignoreIds) {
        if (StringUtils.isBlank(graphId))
            throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");

        int offsets = (StringUtils.isNotBlank(offset))? Integer.parseInt(offset): 0;
        int limits = (StringUtils.isNotBlank(limit))? Integer.parseInt(limit): 0;
        List<String> identifiers = util.getPublishedCollections(graphId, offsets, limits);
        DefinitionDTO definition = util.getDefinition(graphId, "Content");

        if(CollectionUtils.isEmpty(identifiers)) {
            System.out.println("No objects found in this graph.");
        }else {
            System.out.println("No.of collections to be synced : " + identifiers.size());

            System.out.println("-----------------------------------------");

            syncCollections(graphId, identifiers, definition, Arrays.asList(ignoreIds));

        }
    }

    private void syncCollections(String graphId, List<String> identifiers, DefinitionDTO definition, List<String> ignoredIds) {
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
                if(!ignoredIds.contains(node.getIdentifier())){
                    Map<String, Object> hierarchy = util.getHierarchyMap(node.getGraphId(), node.getIdentifier(), definition, null,
                            null);
                    hierarchyStore.saveOrUpdateHierarchy(node.getIdentifier(), hierarchy);
                    status +=1;
                }else{
                    System.out.println("ignoring ID : " + node.getIdentifier());
                }
            }
            //status += ids.size();
            System.out.println("Completed " + status + " collections out of " + identifiers.size());
        }
        System.out.println("-----------------------------------------");
        long endTime = System.currentTimeMillis();
        System.out.println("Sync completed at " + endTime);
        System.out.println("Time taken to sync nodes: " + (endTime - startTime) + "ms");
    }
}
