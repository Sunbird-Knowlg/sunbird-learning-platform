package org.ekstep.kernel.extension;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;
import java.util.Map;

public class ESSync {
    private static CSIndexer csindexer =  new CSIndexer();

    public static void pushDataToES(List<Map<String, Object>> kafkaMessages, GraphDatabaseService graphDb) {

        for(Map<String, Object> message: kafkaMessages) {
            String nodeType = (String) message.get("nodeType");
            String objectType = (String) message.get("objectType");
            String graphId = (String) message.get("graphId");
            String uniqueId = (String) message.get("nodeUniqueId");
            String messageId = (String) message.get("mid");
            try {
                csindexer.processESMessage(graphId, objectType, uniqueId, messageId, message, graphDb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}