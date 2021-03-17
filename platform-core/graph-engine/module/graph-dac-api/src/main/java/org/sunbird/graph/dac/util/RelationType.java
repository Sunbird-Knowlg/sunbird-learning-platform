package org.sunbird.graph.dac.util;

import org.neo4j.graphdb.RelationshipType;

public class RelationType implements RelationshipType {

    private String name;

    public RelationType(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

}
