package com.ilimi.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;

import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;

public class Traverser implements Serializable {

    private static final long serialVersionUID = -8621143933941005381L;
    public static final int BREADTH_FIRST_TRAVERSAL = 0;
    public static final int DEPTH_FIRST_TRAVERSAL = 1;

    private String graphId;
    private GraphDatabaseService graphDb;;

    private Node startNode;
    private int traversal = BREADTH_FIRST_TRAVERSAL;
    private List<RelationTraversal> relations = new ArrayList<RelationTraversal>();
    private int toDepth;
    private int fromDepth;

    private List<Node> endNodes = new ArrayList<Node>();
    private List<String> endRelations = new ArrayList<String>();

    public Traverser(String graphId, String startNodeId) {
        this.graphId = graphId;
        this.graphDb = Neo4jGraphFactory.getGraphDb(graphId);
        Node node = Neo4jGraphUtil.getNodeByUniqueId(graphDb, startNodeId);
        this.startNode = node;
    }

    public Traverser traversal(int traversal) {
        if (traversal == BREADTH_FIRST_TRAVERSAL || traversal == DEPTH_FIRST_TRAVERSAL)
            this.traversal = traversal;
        return this;
    }

    public Traverser traverseRelation(RelationTraversal relationTraversal) {
        if (null != relationTraversal)
            this.relations.add(relationTraversal);
        return this;
    }

    public Traverser fromDepth(int fromDepth) {
        if (fromDepth > 0)
            this.fromDepth = fromDepth;
        return this;
    }

    public Traverser toDepth(int toDepth) {
        if (toDepth > 0)
            this.toDepth = toDepth;
        return this;
    }

    public Traverser endNode(String nodeId) {
        Node node = Neo4jGraphUtil.getNodeByUniqueId(graphDb, nodeId);
        this.endNodes.add(node);
        return this;
    }

    public Traverser endRelations(String relation) {
        this.endRelations.add(relation);
        return this;
    }

    public TraversalDescription getTraversalDescription() {
        TraversalDescription td = graphDb.traversalDescription();
        if (this.traversal == DEPTH_FIRST_TRAVERSAL)
            td = td.depthFirst();
        else
            td = td.breadthFirst();
        if (null != relations && relations.size() > 0) {
            for (RelationTraversal rel : relations) {
                td = rel.addToTraversalDescription(td);
            }
        }
        if (fromDepth > 0) {
            td = td.evaluator(Evaluators.fromDepth(fromDepth));
        }
        if (toDepth > 0) {
            td = td.evaluator(Evaluators.toDepth(toDepth));
        }
        if (null != endNodes && endNodes.size() > 0) {
            Node[] nodeArray = new Node[endNodes.size()];
            for (int i = 0; i < endNodes.size(); i++) {
                nodeArray[i] = endNodes.get(i);
            }
            td = td.evaluator(Evaluators.pruneWhereEndNodeIs(nodeArray));
        }
        if (null != endRelations && endRelations.size() > 0) {
            RelationType type = new RelationType(endRelations.get(0));
            if (endRelations.size() > 1) {
                RelationType[] relationTypes = new RelationType[endRelations.size() - 1];
                for (int i = 1; i < endRelations.size(); i++) {
                    relationTypes[i - 1] = new RelationType(endRelations.get(i));
                }
                td = td.evaluator(Evaluators.pruneWhereLastRelationshipTypeIs(type, relationTypes));
            } else {
                td = td.evaluator(Evaluators.pruneWhereLastRelationshipTypeIs(type));
            }
        }
        return td;
    }

    public SubGraph traverse() {
        SubGraph subGraph = new SubGraph();
        ResourceIterator<Path> iterator = getTraversalDescription().traverse(startNode).iterator();
        if (null != iterator) {
            while (iterator.hasNext()) {
                com.ilimi.graph.dac.model.Path path = new com.ilimi.graph.dac.model.Path(graphId, iterator.next());
                subGraph.addPath(path);
            }
            iterator.close();
        }
        return subGraph;
    }

    public Graph getSubGraph() {
        Graph subGraph = new Graph();
        TraversalDescription td = getTraversalDescription();
        if (null != td) {
            ResourceIterable<Node> nodes = td.traverse(startNode).nodes();
            ResourceIterable<Relationship> relations = td.traverse(startNode).relationships();
            if (null != nodes) {
                List<com.ilimi.graph.dac.model.Node> nodeList = new ArrayList<com.ilimi.graph.dac.model.Node>();
                for (Node dbNode : nodes) {
                    com.ilimi.graph.dac.model.Node node = new com.ilimi.graph.dac.model.Node(graphId, dbNode);
                    nodeList.add(node);
                }
                subGraph.setNodes(nodeList);
            }
            if (null != relations) {
                List<Relation> relationList = new ArrayList<Relation>();
                for (Relationship dbRel : relations) {
                    Relation rel = new Relation(graphId, dbRel);
                    relationList.add(rel);
                }
                subGraph.setRelations(relationList);
            }
        }
        return subGraph;
    }
}
