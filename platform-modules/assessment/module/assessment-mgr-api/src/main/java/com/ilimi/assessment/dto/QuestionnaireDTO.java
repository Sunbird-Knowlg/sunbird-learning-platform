package com.ilimi.assessment.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.assessment.enums.QuestionnaireType;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class QuestionnaireDTO extends Node  {

    private static final long serialVersionUID = 2382516005215431314L;
    
    private List<NodeDTO> concepts;
    private List<String> items;
    private List<Map<String, Object>> item_sets;

    public QuestionnaireDTO() {
    }
    
    public QuestionnaireDTO(Node node) {
        this(node, null);
    }
    
    public QuestionnaireDTO(Node node, String[] qrfields) {
        if (null != node) {
            setGraphId(node.getGraphId());
            setIdentifier(node.getIdentifier());
            setNodeType(node.getNodeType());
            setObjectType(node.getObjectType());
            if (null != qrfields && qrfields.length > 0) {
                if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                    List<String> fields = Arrays.asList(qrfields);
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
//            setInRelations(node.getInRelations());
//            setOutRelations(node.getOutRelations());
            setTags(node.getTags());

            if (null != getOutRelations() && !getOutRelations().isEmpty()) {
                this.concepts = new ArrayList<NodeDTO>();
                for (Relation rel : getOutRelations()) {
                    if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
                        if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
                            this.concepts.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType()));
                        }
                    } else if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.SET.name(), rel.getEndNodeType())) {
                        if (StringUtils.equalsIgnoreCase("AssessmentItem", rel.getEndNodeObjectType())) {
                            String type = (String)node.getMetadata().get("type");
                            if(null != type && QuestionnaireType.materialised.name().equals(type)) {
                                //TODO: update items
                            } else if(null != type && QuestionnaireType.dynamic.name().equals(type)) {
                                Map<String, Object> data = new HashMap<String, Object>();
                                
                            }
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

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<Map<String, Object>> getItem_sets() {
        return item_sets;
    }

    public void setItem_sets(List<Map<String, Object>> item_sets) {
        this.item_sets = item_sets;
    }
    
    public void addItem_set(Map<String, Object> data) {
        if(null == item_sets) 
            item_sets = new ArrayList<Map<String, Object>>();
        item_sets.add(data);
    }

}
