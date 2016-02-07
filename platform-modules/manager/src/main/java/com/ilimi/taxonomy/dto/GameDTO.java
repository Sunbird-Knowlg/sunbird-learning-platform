package com.ilimi.taxonomy.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class GameDTO extends Node {

    private static final long serialVersionUID = -9110500103472852339L;
    private List<NodeDTO> concepts;
    private List<MediaDTO> screenshots;

    public GameDTO() {
    }

    public GameDTO(Node node) {
        this(node, null);
    }

    public GameDTO(Node node, String[] gfields) {
        if (null != node) {
            setGraphId(node.getGraphId());
            setIdentifier(node.getIdentifier());
            setNodeType(node.getNodeType());
            setObjectType(node.getObjectType());
            if (null != gfields && gfields.length > 0) {
                if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                    List<String> fields = Arrays.asList(gfields);
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

            if (null != getOutRelations() && !getOutRelations().isEmpty()) {
                this.concepts = new ArrayList<NodeDTO>();
                for (Relation rel : getOutRelations()) {
                    if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
                        if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
                            this.concepts.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType()));
                        }
                    }
                }
            }
        }
    }

    public List<MediaDTO> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<MediaDTO> screenshots) {
        this.screenshots = screenshots;
    }

    public List<NodeDTO> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<NodeDTO> concepts) {
        this.concepts = concepts;
    }

    public List<String> screenShots() {
        if (null != getOutRelations() && !getOutRelations().isEmpty()) {
            List<String> mediaIds = new ArrayList<String>();
            for (Relation rel : getOutRelations()) {
                if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                        && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
                    if (StringUtils.equalsIgnoreCase("Media", rel.getEndNodeObjectType())) {
                        mediaIds.add(rel.getEndNodeId());
                    }
                }
            }
            return mediaIds;
        }
        return null;
    }
}
