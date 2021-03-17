package org.sunbird.graph.dac.model;

import java.io.Serializable;

import org.sunbird.graph.dac.util.RelationType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.traversal.TraversalDescription;

public class RelationTraversal implements Serializable {

    private static final long serialVersionUID = 4380858539752652736L;
    public static final int DIRECTION_IN = 1;
    public static final int DIRECTION_OUT = 2;
    public static final int DIRECTION_BOTH = 0;

    private String relationName;
    private int direction = DIRECTION_BOTH;

    public RelationTraversal(String relationName) {
        this.relationName = relationName;
    }

    public RelationTraversal(String relationName, int direction) {
        this.relationName = relationName;
        setDirection(direction);
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        if (direction == DIRECTION_IN || direction == DIRECTION_OUT || direction == DIRECTION_BOTH)
            this.direction = direction;
        else
            this.direction = DIRECTION_BOTH;
    }

    public TraversalDescription addToTraversalDescription(TraversalDescription td) {
        if (this.direction == DIRECTION_IN)
            td.relationships(new RelationType(relationName), Direction.INCOMING);
        else if (this.direction == DIRECTION_OUT)
            td.relationships(new RelationType(relationName), Direction.OUTGOING);
        else
            td.relationships(new RelationType(relationName));
        return td;
    }
}
