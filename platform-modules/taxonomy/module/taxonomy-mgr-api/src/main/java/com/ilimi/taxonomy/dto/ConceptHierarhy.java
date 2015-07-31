package com.ilimi.taxonomy.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class ConceptHierarhy {

	private String identifier;
	private String objectType;
	private String type;
	private Map<String, Object> metadata = new HashMap<String, Object>();
	private List<ConceptHierarhy> children;

	public ConceptHierarhy(Node parent, Map<String, Node> nodes, String[] cfields) {
		setIdentifier(parent.getIdentifier());
		setObjectType(parent.getObjectType());
		setType(parent); // TODO: We need to change this in future.
		setMetadata(parent, cfields);
		setChildren(parent, nodes, cfields);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	private void setMetadata(Node parent, String[] cfields) {
		if (null != cfields && cfields.length > 0) {
            if (null != parent.getMetadata() && !parent.getMetadata().isEmpty()) {
                List<String> fields = Arrays.asList(cfields);
                for (Entry<String, Object> entry : parent.getMetadata().entrySet()) {
                    if (fields.contains(entry.getKey()))
                        metadata.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            this.metadata = parent.getMetadata();
        }
	}

	public List<ConceptHierarhy> getChildren() {
		return children;
	}

	private void setChildren(Node parent, Map<String, Node> nodes, String[] cfields) {
		List<Relation> relations = parent.getOutRelations();
		if(null != relations && !relations.isEmpty()) {
			this.children = new ArrayList<ConceptHierarhy>();
			for(Relation relation : relations) {
				if(SystemNodeTypes.DATA_NODE.name().equals(relation.getEndNodeType()) && "Concept".equals(relation.getEndNodeObjectType())) {
					Node child = nodes.get(relation.getEndNodeId());
					this.children.add(new ConceptHierarhy(child, nodes, cfields));
				}
			}
		}
	}

	public String getType() {
		return type;
	}

	public void setType(Node node) {
		if("Concept".equals(node.getObjectType()))
			this.type = node.getIdentifier().substring(0, 2);
	}

}
