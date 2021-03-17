package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.List;

public class Graph implements Serializable {

    private static final long serialVersionUID = -5174004735080378302L;
    private List<Node> nodes;
    private List<Relation> relations;

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
}
