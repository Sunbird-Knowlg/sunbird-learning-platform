package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Relationship;

public class Path implements Serializable {

    private static final long serialVersionUID = 2484883859842926325L;
    private String graphId;
    private Node startNode;
    private Node endNode;
    private List<Node> nodes;
    private List<Relation> relations;
    
    public Path(String graphId) {
    	this.setGraphId(graphId);
    }

    public Path(String graphId, org.neo4j.graphdb.Path dbPath) {
        if (null != dbPath) {
            this.startNode = new Node(graphId, dbPath.startNode());
            this.endNode = new Node(graphId, dbPath.endNode());
            this.nodes = new LinkedList<Node>();
            Iterable<org.neo4j.graphdb.Node> pathNodes = dbPath.nodes();
            if (null != pathNodes) {
                for (org.neo4j.graphdb.Node pathNode : pathNodes) {
                    this.nodes.add(new Node(graphId, pathNode));
                }
            }

            this.relations = new LinkedList<Relation>();
            Iterable<Relationship> pathRelations = dbPath.relationships();
            if (null != pathRelations) {
                for (Relationship pathRelation : pathRelations) {
                    this.relations.add(new Relation(graphId, pathRelation));
                }
            }
        }
    }

    /**
     * Returns the length of this path. That is the number of relations (which
     * is the same as the number of nodes minus one). The shortest path possible
     * is of length 0.
     * 
     * @return the length (i.e. the number of relations) in the path.
     */
    public int length() {
        return (null == relations) ? 0 : relations.size();
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

	public String getGraphId() {
		return graphId;
	}

	public void setGraphId(String graphId) {
		this.graphId = graphId;
	}
}
