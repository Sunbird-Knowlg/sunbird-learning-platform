package com.ilimi.assessment.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import com.ilimi.assessment.enums.AssessmentErrorCodes;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;

public class ItemSetDTO extends Node {

    private static final long serialVersionUID = -6225438811177155504L;
    private String MEMBER_IDS_KEY = "memberIds";
    private String CRITERIA_KEY = "criteria";
    ObjectMapper mapper = new ObjectMapper();
    private SearchCriteria criteria;
    private List<String> memberIds;
    private List<NodeDTO> concepts;
    
    @SuppressWarnings("unused")
    private ItemSetDTO() {
    }
     
    public ItemSetDTO(Node node) {
        this(node, null);
    }
    
    public ItemSetDTO(Node node, String[] ifields) {
        if (null != node) {
            setGraphId(node.getGraphId());
            setIdentifier(node.getIdentifier());
            setNodeType(node.getNodeType());
            setObjectType(node.getObjectType());
            setMemberIds(node.getMetadata());
            
            try {
                String strCriteria = (String) node.getMetadata().get(CRITERIA_KEY);
                if(StringUtils.isNotBlank(strCriteria)) {
                    ItemSearchCriteria itemSearchCriteria = mapper.readValue(strCriteria, ItemSearchCriteria.class);
                    setCriteria(itemSearchCriteria.getSearchCriteria());
                }
            } catch (Exception e) {
                throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_SEARCH_CRITERIA.name(), "Criteria given to create ItemSet is invalid.");
            }
            
            if (null != ifields && ifields.length > 0) {
                if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                    List<String> fields = Arrays.asList(ifields);
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
                for (Relation rel : node.getOutRelations()) {
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
    
    @JsonIgnore
    public SearchCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @JsonIgnore
    public List<String> getMemberIds() {
        return memberIds;
    }

    @SuppressWarnings("unchecked")
    private void setMemberIds(Map<String, Object> metadata) {
        Object obj = metadata.get(MEMBER_IDS_KEY);
        if(null != obj) {
            metadata.remove(MEMBER_IDS_KEY);
            this.memberIds = mapper.convertValue(obj, List.class);
        }
    }

    public List<NodeDTO> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<NodeDTO> concepts) {
        this.concepts = concepts;
    }
    
}
