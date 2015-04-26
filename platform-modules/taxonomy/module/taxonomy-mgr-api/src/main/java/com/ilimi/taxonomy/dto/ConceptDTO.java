package com.ilimi.taxonomy.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class ConceptDTO extends Node {

    private static final long serialVersionUID = -5820790915284632780L;

    public ConceptDTO() {
    }

    public ConceptDTO(Node node) {
        this(node, null);
    }

    public ConceptDTO(Node node, String[] cfields) {
        if (null != node) {
            setGraphId(node.getGraphId());
            setIdentifier(node.getIdentifier());
            setNodeType(node.getNodeType());
            setObjectType(node.getObjectType());
            if (null != cfields && cfields.length > 0) {
                if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                    List<String> fields = Arrays.asList(cfields);
                    Map<String, Object> metadata = new HashMap<String, Object>();
                    for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
                        if (fields.contains(entry.getKey()))
                            metadata.put(entry.getKey(), entry.getValue());
                    }
                    setMetadata(metadata);
                }
            } else {
                setMetadata(node.getMetadata());
            }
            setInRelations(node.getInRelations());
            setOutRelations(node.getOutRelations());
            setTags(node.getTags());

            if (null != getInRelations() && !getInRelations().isEmpty()) {
                this.games = new ArrayList<NodeDTO>();
                for (Relation rel : getInRelations()) {
                    if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getStartNodeType())) {
                        if (StringUtils.equalsIgnoreCase("Game", rel.getStartNodeObjectType())) {
                            this.games.add(new NodeDTO(rel.getStartNodeId(), rel.getStartNodeName(), rel.getStartNodeObjectType()));
                        }
                    }
                }
            }
        }
    }

    private List<NodeDTO> games;

    public List<NodeDTO> getGames() {
        return games;
    }

    public void setGames(List<NodeDTO> games) {
        this.games = games;
    }
}
