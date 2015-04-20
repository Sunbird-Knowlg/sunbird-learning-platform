package com.ilimi.graph.dac.util;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import com.ilimi.graph.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;

public class Neo4jGraphUtil {

    public static final Label NODE_LABEL = DynamicLabel.label("NODE");

    public static Node getNodeByUniqueId(GraphDatabaseService graphDb, String nodeId) {
        Transaction tx = graphDb.beginTx();
        ResourceIterator<Node> startNodes = graphDb.findNodes(NODE_LABEL, SystemProperties.IL_UNIQUE_ID.name(), nodeId);
        tx.success();
        Node node = null;
        if (null != startNodes && startNodes.hasNext()) {
            node = startNodes.next();
            startNodes.close();
        } else {
            throw new ResourceNotFoundException(GraphDACErrorCodes.ERR_DAC_NODE_NOT_FOUND_005.name(), "Node not found: " + nodeId);
        }
        tx.close();
        return node;
    }

    public static Relationship getRelationship(GraphDatabaseService graphDb, String startNodeId, String relationType, String endNodeId) {
        Node startNode = getNodeByUniqueId(graphDb, startNodeId);
        RelationType relType = new RelationType(relationType);
        Iterable<Relationship> relations = startNode.getRelationships(Direction.OUTGOING, relType);
        if (null != relations) {
            for (Relationship rel : relations) {
                Object relEndNodeId = rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
                String strEndNodeId = (null == relEndNodeId) ? null : relEndNodeId.toString();
                if (StringUtils.equals(endNodeId, strEndNodeId)) {
                    return rel;
                }
            }
        }
        return null;
    }
}
