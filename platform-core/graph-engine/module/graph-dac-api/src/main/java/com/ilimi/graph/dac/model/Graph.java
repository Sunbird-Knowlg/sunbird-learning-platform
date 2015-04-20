package com.ilimi.graph.dac.model;

import java.util.List;

import com.ilimi.graph.common.dto.BaseValueObject;

public class Graph extends BaseValueObject {

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
