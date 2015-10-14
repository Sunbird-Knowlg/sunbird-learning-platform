package com.ilimi.taxonomy.dto;

import java.io.Serializable;
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

public class ContentDTO implements Serializable {

    private static final long serialVersionUID = -4400561249191832076L;
    private String identifier;
    private List<NodeDTO> concepts;
    private List<NodeDTO> questionnaires;
    private List<String> tags;
    private Map<String, Object> metadata = new HashMap<String, Object>();

    public ContentDTO() {
    }

    public ContentDTO(Node node) {
        this(node, null);
    }

    public ContentDTO(Node node, String[] wfields) {
        if (null != node) {
            setIdentifier(node.getIdentifier());
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
            setTags(node.getTags());
            if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
                this.concepts = new ArrayList<NodeDTO>();
                this.questionnaires = new ArrayList<NodeDTO>();
                for (Relation rel : node.getOutRelations()) {
                    if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
                        if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
                            this.concepts.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(),
                                    rel.getEndNodeObjectType(), rel.getRelationType()));
                        } else if (StringUtils.equalsIgnoreCase("Questionnaire", rel.getEndNodeObjectType())) {
                            this.questionnaires.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(),
                                    rel.getEndNodeObjectType(), rel.getRelationType()));
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
        if (null != concepts && !concepts.isEmpty()) {
            returnMap.put("concepts", concepts);
        }
        if (null != questionnaires && !questionnaires.isEmpty()) {
            returnMap.put("questionnaires", concepts);
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

    public List<NodeDTO> getQuestionnaires() {
        return questionnaires;
    }

    public void setQuestionnaires(List<NodeDTO> questionnaires) {
        this.questionnaires = questionnaires;
    }

}
