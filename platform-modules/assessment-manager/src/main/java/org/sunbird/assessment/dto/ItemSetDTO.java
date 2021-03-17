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
import org.sunbird.graph.model.collection.Set;

public class ItemSetDTO implements Serializable {

    private static final long serialVersionUID = -6225438811177155504L;

    private String subject;
    private String identifier;
    private List<NodeDTO> concepts;
    private List<String> tags;
    private List<String> items;
    private List<NodeDTO> questionnaires;
    private Map<String, Object> metadata = new HashMap<String, Object>();

    @SuppressWarnings("unused")
    private ItemSetDTO() {
    }

    public ItemSetDTO(Node node) {
        this(node, null, null, null);
    }

    public ItemSetDTO(Node node, List<String> items, String[] ifields, List<String> jsonProps) {
        if (null != node) {
            setIdentifier(node.getIdentifier());
            setSubject(node.getGraphId());
            Map<String, Object> nodeMetadata = node.getMetadata();
            if (null != nodeMetadata && !nodeMetadata.isEmpty()) {
                if (null != ifields && ifields.length > 0) {
                    List<String> fields = Arrays.asList(ifields);
                    Map<String, Object> metadata = new HashMap<String, Object>();
                    for (Entry<String, Object> entry : nodeMetadata.entrySet()) {
                        if (fields.contains(entry.getKey())) {
                            if (jsonProps.contains(entry.getKey())) {
                                Object val = JSONUtils.convertJSONString((String) entry.getValue());
                                if (null != val)
                                    metadata.put(entry.getKey(), val);
                            } else {
                                metadata.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    setMetadata(metadata);
                } else {
                    for (Entry<String, Object> entry : nodeMetadata.entrySet()) {
                        if (null != entry.getValue()) {
                            if (jsonProps.contains(entry.getKey())) {
                                Object val = JSONUtils.convertJSONString((String) entry.getValue());
                                if (null != val)
                                    metadata.put(entry.getKey(), val);
                            } else {
                                metadata.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
            }
            setTags(node.getTags());
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
            if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
                this.questionnaires = new ArrayList<NodeDTO>();
                for (Relation rel : node.getInRelations()) {
                    if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getStartNodeType())) {
                        if (StringUtils.equalsIgnoreCase("Questionnaire", rel.getStartNodeObjectType())) {
                            this.questionnaires.add(new NodeDTO(rel.getStartNodeId(), rel.getStartNodeName(),
                                    rel.getStartNodeObjectType()));
                        }
                    }
                }
            }
            setItems(items);
        }
    }

    public Map<String, Object> returnMap() {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.putAll(getMetadata());
        returnMap.remove(Set.SET_CRITERIA_KEY);
        returnMap.remove(Set.SET_CRITERIA_QUERY_KEY);
        returnMap.put("identifier", getIdentifier());
        returnMap.put("subject", getSubject());
        if (null != items && !items.isEmpty()) {
            returnMap.put("items", items);
        }
        if (null != concepts && !concepts.isEmpty()) {
            returnMap.put("concepts", concepts);
        }
        if (null != tags && !tags.isEmpty()) {
            returnMap.put("tags", tags);
        }
        if (null != questionnaires && !questionnaires.isEmpty()) {
            returnMap.put("questionnaires", questionnaires);
        }
        return returnMap;
    }

    public List<NodeDTO> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<NodeDTO> concepts) {
        this.concepts = concepts;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

}
