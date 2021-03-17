package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.exception.GraphDACErrorCodes;
import org.neo4j.driver.v1.Value;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

public class Node implements Serializable {

	private static final long serialVersionUID = 252337826576516976L;
	private long id;
	private String graphId;
	private String identifier;
	private String nodeType;
	private String objectType;
	private Map<String, Object> metadata;
	private List<Relation> outRelations;
	private List<Relation> inRelations;

	public Node() {

	}

	public Node(String identifier, String nodeType, String objectType) {
		this.identifier = identifier;
		this.nodeType = nodeType;
		this.objectType = objectType;
	}

	public Node(String graphId, Map<String, Object> metadata) {
		this.graphId = graphId;
		this.metadata = metadata;
		if (null != metadata && !metadata.isEmpty()) {
			if (null != metadata.get(SystemProperties.IL_UNIQUE_ID.name()))
				this.identifier = metadata.get(SystemProperties.IL_UNIQUE_ID.name()).toString();
			if (null != metadata.get(SystemProperties.IL_SYS_NODE_TYPE.name()))
				this.nodeType = metadata.get(SystemProperties.IL_SYS_NODE_TYPE.name()).toString();
			if (null != metadata.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
				this.objectType = metadata.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).toString();
		}
	}

	public Node(String graphId, org.neo4j.driver.v1.types.Node node, Map<Long, Object> relationMap,
			Map<Long, Object> startNodeMap, Map<Long, Object> endNodeMap) {
		if (null == node)
			throw new ServerException(GraphDACErrorCodes.ERR_GRAPH_NULL_DB_NODE.name(),
					"Failed to create node object. Node from database is null.");

		this.graphId = graphId;
		this.id = node.id();
		Iterable<String> keys = node.keys();
		if (null != keys && null != keys.iterator()) {
			this.metadata = new HashMap<String, Object>();
			for (String key : keys) {
				if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_UNIQUE_ID.name()))
					this.identifier = node.get(key).asString();
				else if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_SYS_NODE_TYPE.name()))
					this.nodeType = node.get(key).asString();
				else if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
					this.objectType = node.get(key).asString();
				else {
					Value value = node.get(key);
					if (null != value) {
						if (StringUtils.startsWithIgnoreCase(value.type().name(), "LIST")) {
							List<Object> list = value.asList();
							if (null != list && list.size() > 0) {
								Object obj = list.get(0);
								if (obj instanceof String) {
									this.metadata.put(key, list.toArray(new String[0]));
								} else if (obj instanceof Number) {
									this.metadata.put(key, list.toArray(new Number[0]));
								} else if (obj instanceof Boolean) {
									this.metadata.put(key, list.toArray(new Boolean[0]));
								} else {
									this.metadata.put(key, list.toArray(new Object[0]));
								}
							}
						} else
							this.metadata.put(key, value.asObject());
					}
				}
					
			}
		}

		if (null != relationMap && !relationMap.isEmpty() && null != startNodeMap && !startNodeMap.isEmpty()
				&& null != endNodeMap && !endNodeMap.isEmpty()) {
			this.inRelations = new ArrayList<Relation>();
			this.outRelations = new ArrayList<Relation>();
//			this.tags = new ArrayList<String>();

			for (Entry<Long, Object> entry : relationMap.entrySet()) {
				org.neo4j.driver.v1.types.Relationship relationship = (org.neo4j.driver.v1.types.Relationship) entry
						.getValue();
				if (relationship.startNodeId() == node.id()) {
					Relation rel = new Relation(graphId, relationship, startNodeMap, endNodeMap);
					this.outRelations.add(rel);
				} if (relationship.endNodeId() == node.id()) {
					Relation rel = new Relation(graphId, relationship, startNodeMap, endNodeMap);
					if (!isTagRelation(rel))
						this.inRelations.add(rel);
				}
			}
		}
	}

	public Node(String graphId, org.neo4j.graphdb.Node neo4jNode) {
		if (null == neo4jNode)
			throw new ServerException(GraphDACErrorCodes.ERR_GRAPH_NULL_DB_NODE.name(),
					"Failed to create node object. Node from database is null.");
		this.graphId = graphId;
		this.id = neo4jNode.getId();
		Iterable<String> keys = neo4jNode.getPropertyKeys();
		if (null != keys && null != keys.iterator()) {
			this.metadata = new HashMap<String, Object>();
			for (String key : keys) {
				if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_UNIQUE_ID.name()))
					this.identifier = neo4jNode.getProperty(key).toString();
				else if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_SYS_NODE_TYPE.name()))
					this.nodeType = neo4jNode.getProperty(key).toString();
				else if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
					this.objectType = neo4jNode.getProperty(key).toString();
				else
					this.metadata.put(key, neo4jNode.getProperty(key));
			}
		}
		Iterable<Relationship> outRels = neo4jNode.getRelationships(Direction.OUTGOING);
		if (null != outRels && null != outRels.iterator()) {
			this.outRelations = new ArrayList<Relation>();
			for (Relationship outRel : outRels)
				this.outRelations.add(new Relation(graphId, outRel));
		}
		Iterable<Relationship> inRels = neo4jNode.getRelationships(Direction.INCOMING);
		if (null != inRels && null != inRels.iterator()) {
			this.inRelations = new ArrayList<Relation>();
			for (Relationship inRel : inRels) {
				Relation rel = new Relation(graphId, inRel);
			}
		}
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getIdentifier() {
		if (StringUtils.isBlank(identifier) && null != metadata)
			this.identifier = (String) metadata.get(SystemProperties.IL_UNIQUE_ID.name());
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@JsonIgnore
	public String getGraphId() {
		return graphId;
	}

	public void setGraphId(String graphId) {
		this.graphId = graphId;
	}

	@JsonIgnore
	public String getNodeType() {
		if (StringUtils.isBlank(nodeType) && null != metadata)
			this.nodeType = (String) metadata.get(SystemProperties.IL_SYS_NODE_TYPE.name());
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getObjectType() {
		if (StringUtils.isBlank(objectType) && null != metadata)
			this.objectType = (String) metadata.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public List<Relation> getOutRelations() {
		return outRelations;
	}

	public void setOutRelations(List<Relation> outRelations) {
		this.outRelations = outRelations;
	}

	public List<Relation> getInRelations() {
		return inRelations;
	}

	public void setInRelations(List<Relation> inRelations) {
		this.inRelations = inRelations;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	public List<String> getTags() {
		if (null == this.metadata) 
			return null;
		else {
			Object keywords = this.metadata.get("keywords");
			if (keywords instanceof String)
				return Arrays.asList((String)keywords);
			else  if (keywords instanceof String[])
				return Arrays.asList((String[]) keywords);
			else return (List<String>) keywords;
		}
 	}
 
 	public void setTags(List<String> tags) {
 		if (null == this.metadata)
 			this.metadata = new HashMap<>();
 		this.metadata.put("keywords", tags);
 	}
	
	private boolean isTagRelation(Relation rel) {
		if (StringUtils.equals("TAG", rel.getStartNodeType())
				&& StringUtils.equals(RelationTypes.SET_MEMBERSHIP.relationName(), rel.getRelationType()))
			return true;
		return false;
	}
}
