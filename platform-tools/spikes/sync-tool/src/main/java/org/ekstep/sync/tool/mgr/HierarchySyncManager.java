package org.ekstep.sync.tool.mgr;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component("hierarchySyncManager")
public class HierarchySyncManager {

    private ControllerUtil util = new ControllerUtil();

    private CollectionStore collectionStore = new CollectionStore();

    private static final int BATCH_SIZE = 50;

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
        int current = 0;
        long startTime = System.currentTimeMillis();

        for(List<String> ids : Lists.partition(identifiers, (identifiers.size()/BATCH_SIZE))) {
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

                //printProgress(startTime, (long) ids.size(), (long) current);
            }
        }
        System.out.println("-----------------------------------------");
        long endTime = System.currentTimeMillis();
        System.out.println("Sync completed at " + endTime);
        System.out.println("Time taken to sync nodes: " + (endTime - startTime) + "ms");
    }


    private static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print(string);
    }


}
