package org.sunbird.assessment.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.NodeDTO;
import org.sunbird.graph.common.JSONUtils;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;

public class QuestionnaireDTO implements Serializable {

    private static final long serialVersionUID = 2382516005215431314L;

    private String subject;
    private String identifier;
    private List<NodeDTO> concepts;
    private Object itemSets;
    private List<String> tags;
    private Map<String, Object> metadata = new HashMap<String, Object>();

    public QuestionnaireDTO() {
    }

    public QuestionnaireDTO(Node node) {
        this(node, null);
    }

    public QuestionnaireDTO(Node node, String[] qrfields) {
        if (null != node) {
            setSubject(node.getGraphId());
            setIdentifier(node.getIdentifier());
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
            setTags(node.getTags());
            if (getMetadata().containsKey("item_sets")) {
                String itemSets = (String) getMetadata().get("item_sets");
                Object obj = JSONUtils.convertJSONString(itemSets);
                setItemSets(obj);
                getMetadata().remove("item_sets");
            }
            if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
                this.concepts = new ArrayList<NodeDTO>();
                for (Relation rel : node.getOutRelations()) {
                    if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
                        if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
                            this.concepts.add(
                                    new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType()));
                        }
                    }
                }
            }
        }
    }

    public Map<String, Object> returnMap() {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.putAll(getMetadata());
        returnMap.put("identifier", getIdentifier());
        returnMap.put("subject", getSubject());
        if (null != itemSets) {
            returnMap.put("item_sets", itemSets);
        }
        if (null != concepts && !concepts.isEmpty()) {
            returnMap.put("concepts", concepts);
        }
        if (null != tags && !tags.isEmpty()) {
            returnMap.put("tags", tags);
        }
        return returnMap;
    }

    public List<NodeDTO> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<NodeDTO> concepts) {
        this.concepts = concepts;
    }

    public Object getItemSets() {
        return itemSets;
    }

    public void setItemSets(Object itemSets) {
        this.itemSets = itemSets;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, Object> getMetadata() {
        if (null == metadata)
            metadata = new HashMap<String, Object>();
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

}
