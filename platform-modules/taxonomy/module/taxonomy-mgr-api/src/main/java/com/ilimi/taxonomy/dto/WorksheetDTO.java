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

public class WorksheetDTO extends Node {

    private static final long serialVersionUID = -4400561249191832076L;
    private List<NodeDTO> concepts;
    private NodeDTO questionnaire;
    
    public WorksheetDTO() {
    }

    public WorksheetDTO(Node node) {
        this(node, null);
    }

    public WorksheetDTO(Node node, String[] wfields) {
        if (null != node) {
            setGraphId(node.getGraphId());
            setIdentifier(node.getIdentifier());
            setNodeType(node.getNodeType());
            setObjectType(node.getObjectType());
            if (null != wfields && wfields.length > 0) {
                if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                    List<String> fields = Arrays.asList(wfields);
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

            if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
                this.concepts = new ArrayList<NodeDTO>();
                for (Relation rel : node.getOutRelations()) {
                    if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
                        if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
                            this.concepts.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType()));
                        } else if(StringUtils.equalsIgnoreCase("Questionnaire", rel.getEndNodeObjectType())) {
                            questionnaire = new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType());
                        }
                    }
                }
            }
        }
    }
    
    public List<NodeDTO> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<NodeDTO> concepts) {
        this.concepts = concepts;
    }

    public NodeDTO getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(NodeDTO questionnaire) {
        this.questionnaire = questionnaire;
    }
    
}
