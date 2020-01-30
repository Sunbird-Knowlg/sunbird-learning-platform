package org.ekstep.kernel.extension;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.dac.enums.AuditProperties;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.telemetry.util.LogAsyncGraphEvent;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.TransactionData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProcessTransactionData {

	protected String graphId;
	protected GraphDatabaseService graphDb;
	private String nodeLabel = "NODE";

	public ProcessTransactionData(String graphId, GraphDatabaseService graphDb) {
		this.graphId = graphId;
		this.graphDb = graphDb;
	}

	public void processTxnData(TransactionData data) {
		try {
			List<Map<String, Object>> kafkaMessages = getMessageObj(data);
			if (kafkaMessages != null && !kafkaMessages.isEmpty()) {
				LogAsyncGraphEvent.pushMessageToLogger(kafkaMessages);
				ESSync.pushDataToES(kafkaMessages, graphDb);
			}
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
		}
	}

	private String getGraphId(Node node) {
		for (org.neo4j.graphdb.Label lable : node.getLabels()) {
			if (!lable.name().equals(nodeLabel)) {
				return lable.name();
			}
		}
		return this.graphId;
	}

	private String getGraphId(Iterable<LabelEntry> labels) {
		for (org.neo4j.graphdb.event.LabelEntry lable : labels) {
			if (!lable.label().name().equals(nodeLabel)) {
				return lable.label().name();
			}
		}
		return this.graphId;
	}

	private List<Map<String, Object>> getMessageObj(TransactionData data) {
		String userId = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.USER_ID.name());
		String requestId = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.REQUEST_ID.name());
		List<Map<String, Object>> messageMap = new ArrayList<Map<String, Object>>();
		messageMap.addAll(getCretedNodeMessages(data, graphDb, userId, requestId));
		messageMap.addAll(getUpdatedNodeMessages(data, graphDb, userId, requestId));
		messageMap.addAll(getDeletedNodeMessages(data, graphDb, userId, requestId));
		messageMap.addAll(getAddedTagsMessage(data, graphDb, userId, requestId));
		messageMap.addAll(getRemovedTagsMessage(data, graphDb, userId, requestId));
		messageMap.addAll(getRemovedRelationShipMessages(data, userId, requestId));
		messageMap.addAll(getAddedRelationShipMessages(data, userId, requestId));
		return messageMap;
	}

	private List<Map<String, Object>> getCretedNodeMessages(TransactionData data, GraphDatabaseService graphDb,
			String userId, String requestId) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		try {
			List<Long> createdNodeIds = getCreatedNodeIds(data);
			for (Long nodeId : createdNodeIds) {
				// Map<String, Object> map = new HashMap<String, Object>();
				Map<String, Object> transactionData = new HashMap<String, Object>();
				Map<String, Object> propertiesMap = getAssignedNodePropertyEntry(nodeId, data);
				if (null != propertiesMap && !propertiesMap.isEmpty()) {
					transactionData.put(GraphDACParams.properties.name(), propertiesMap);
					Map<String, Object> map = setMessageData(graphDb, nodeId, userId, requestId,
							GraphDACParams.CREATE.name(), transactionData);
					lstMessageMap.add(map);
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error building created nodes message" + e.getMessage(), e);
		}
		return lstMessageMap;
	}

	private List<Map<String, Object>> getUpdatedNodeMessages(TransactionData data, GraphDatabaseService graphDb,
			String userId, String requestId) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		try {
			List<Long> updatedNodeIds = getUpdatedNodeIds(data);
			for (Long nodeId : updatedNodeIds) {
				Map<String, Object> transactionData = new HashMap<String, Object>();
				Map<String, Object> propertiesMap = getAllPropertyEntry(nodeId, data);
				if (null != propertiesMap && !propertiesMap.isEmpty()) {
					String lastUpdatedBy = getLastUpdatedByValue(nodeId, data);
					transactionData.put(GraphDACParams.properties.name(), propertiesMap);
					if (StringUtils.isNotBlank(lastUpdatedBy)) {
						userId = lastUpdatedBy;
					} else {
						userId = "ANONYMOUS";
					}
					Map<String, Object> map = setMessageData(graphDb, nodeId, userId, requestId,
							GraphDACParams.UPDATE.name(), transactionData);
					lstMessageMap.add(map);
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error building updated nodes message" + e.getMessage(), e);
		}
		return lstMessageMap;
	}

	@SuppressWarnings("rawtypes")
	private List<Map<String, Object>> getDeletedNodeMessages(TransactionData data, GraphDatabaseService graphDb,
			String userId, String requestId) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		try {
			List<Long> deletedNodeIds = getDeletedNodeIds(data);
			for (Long nodeId : deletedNodeIds) {
				Map<String, Object> map = new HashMap<String, Object>();
				Map<String, Object> transactionData = new HashMap<String, Object>();
				Map<String, Object> removedNodeProp = getRemovedNodePropertyEntry(nodeId, data);
				if (null != removedNodeProp && !removedNodeProp.isEmpty()) {
					transactionData.put(GraphDACParams.properties.name(), removedNodeProp);
					map.put(GraphDACParams.requestId.name(), requestId);
					if (StringUtils.isEmpty(userId)) {
						if (removedNodeProp.containsKey("lastUpdatedBy"))
							// oldvalue of lastUpdatedBy from the transaction
							// data as node is deleted
							userId = (String) ((Map) removedNodeProp.get("lastUpdatedBy")).get("ov");
						else
							userId = "ANONYMOUS";
					}
					map.put(GraphDACParams.userId.name(), userId);
					map.put(GraphDACParams.operationType.name(), GraphDACParams.DELETE.name());
					map.put(GraphDACParams.label.name(), getLabel(removedNodeProp));
					map.put(GraphDACParams.graphId.name(), getGraphId(data.removedLabels()));
					map.put(GraphDACParams.nodeGraphId.name(), nodeId);
					map.put(GraphDACParams.createdOn.name(), DateUtils.format(new Date()));
					map.put(GraphDACParams.ets.name(), System.currentTimeMillis());
					map.put(GraphDACParams.nodeUniqueId.name(),
							((Map) removedNodeProp.get(SystemProperties.IL_UNIQUE_ID.name())).get("ov"));
					map.put(GraphDACParams.objectType.name(),
							((Map) removedNodeProp.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name())).get("ov"));
					map.put(GraphDACParams.nodeType.name(),
							((Map) removedNodeProp.get(SystemProperties.IL_SYS_NODE_TYPE.name())).get("ov"));
					map.put(GraphDACParams.channel.name(),
							((Map) removedNodeProp.get(GraphDACParams.channel.name())).get("ov"));
					map.put(GraphDACParams.transactionData.name(), transactionData);
					map.put(GraphDACParams.mid.name(), getUUID());
					lstMessageMap.add(map);
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error building deleted nodes message" + e.getMessage(), e);
		}
		return lstMessageMap;
	}

	private Map<String, Object> getAllPropertyEntry(Long nodeId, TransactionData data) {
		Map<String, Object> map = getAssignedNodePropertyEntry(nodeId, data);
		map.putAll(getRemovedNodePropertyEntry(nodeId, data));
		return map;
	}

	private Map<String, Object> getAssignedNodePropertyEntry(Long nodeId, TransactionData data) {
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		return getNodePropertyEntry(nodeId, assignedNodeProp);
	}

	private String getLastUpdatedByValue(Long nodeId, TransactionData data) {
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe : assignedNodeProp) {
			if (nodeId == pe.entity().getId()) {
				if (StringUtils.equalsIgnoreCase("lastUpdatedBy", (String) pe.key())) {
					String lastUpdatedBy = (String) pe.value();
					return lastUpdatedBy;
				}
			}
		}
		return null;
	}

	private Map<String, Object> getRemovedNodePropertyEntry(Long nodeId, TransactionData data) {
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.removedNodeProperties();
		return getNodeRemovedPropertyEntry(nodeId, removedNodeProp);
	}

	private Map<String, Object> getNodePropertyEntry(Long nodeId,
			Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> nodeProp) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe : nodeProp) {
			if (nodeId == pe.entity().getId()) {
				if (!compareValues(pe.previouslyCommitedValue(), pe.value())) {
					Map<String, Object> valueMap = new HashMap<String, Object>();
					valueMap.put("ov", pe.previouslyCommitedValue()); // old
																		// value
					valueMap.put("nv", pe.value()); // new value
					map.put((String) pe.key(), valueMap);
				}
			}
		}
		if (map.size() == 1 && null != map.get(AuditProperties.lastUpdatedOn.name()))
			map = new HashMap<String, Object>();
		return map;
	}

	private Map<String, Object> getNodeRemovedPropertyEntry(Long nodeId,
			Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> nodeProp) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe : nodeProp) {
			if (nodeId == pe.entity().getId()) {
				Map<String, Object> valueMap = new HashMap<String, Object>();
				valueMap.put("ov", pe.previouslyCommitedValue()); // old value
				valueMap.put("nv", null); // new value
				map.put((String) pe.key(), valueMap);
			}
		}
		if (map.size() == 1 && null != map.get(AuditProperties.lastUpdatedOn.name()))
			map = new HashMap<String, Object>();
		return map;
	}

	@SuppressWarnings("rawtypes")
	private boolean compareValues(Object o1, Object o2) {
		if (null == o1)
			o1 = "";
		if (null == o2)
			o2 = "";
		if (o1.equals(o2))
			return true;
		else {
			if (o1 instanceof List) {
				if (!(o2 instanceof List))
					return false;
				else
					return compareLists((List) o1, (List) o2);
			} else if (o1 instanceof Object[]) {
				if (!(o2 instanceof Object[]))
					return false;
				else
					return compareArrays((Object[]) o1, (Object[]) o2);
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean compareLists(List l1, List l2) {
		if (l1.size() != l2.size())
			return false;
		for (int i = 0; i < l1.size(); i++) {
			Object v1 = l1.get(i);
			Object v2 = l2.get(i);
			if ((null == v1 && null != v2) || (null != v1 && null == v2))
				return false;
			if (null != v1 && null != v2 && !v1.equals(v2))
				return false;
		}
		return true;
	}

	private boolean compareArrays(Object[] l1, Object[] l2) {
		if (l1.length != l2.length)
			return false;
		for (int i = 0; i < l1.length; i++) {
			Object v1 = l1[i];
			Object v2 = l2[i];
			if ((null == v1 && null != v2) || (null != v1 && null == v2))
				return false;
			if (null != v1 && null != v2 && !v1.equals(v2))
				return false;
		}
		return true;
	}

	private List<Map<String, Object>> getAddedTagsMessage(TransactionData data, GraphDatabaseService graphDb,
			String userId, String requestId) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		try {
			Iterable<Relationship> createdRelations = data.createdRelationships();
			if (null != createdRelations) {
				for (Relationship rel : createdRelations) {
					if (StringUtils.equalsIgnoreCase(
							rel.getStartNode().getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(),
							GraphDACParams.TAG.name())) {
						if (rel.getStartNode().hasProperty(SystemProperties.IL_TAG_NAME.name())) {
							Map<String, Object> transactionData = new HashMap<String, Object>();
							List<String> tags = new ArrayList<String>();
							transactionData.put(GraphDACParams.properties.name(), new HashMap<String, Object>());
							transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
							tags.add(rel.getStartNode().getProperty(SystemProperties.IL_TAG_NAME.name()).toString());
							transactionData.put(GraphDACParams.addedTags.name(), tags);
							Map<String, Object> map = setMessageData(graphDb, rel.getEndNode().getId(), userId,
									requestId, GraphDACParams.UPDATE.name(), transactionData);
							lstMessageMap.add(map);
						}
					}
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error building added tags message", e);
		}
		return lstMessageMap;
	}

	private List<Map<String, Object>> getRemovedTagsMessage(TransactionData data, GraphDatabaseService graphDb,
			String userId, String requestId) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		try {
			Iterable<Relationship> createdRelations = data.deletedRelationships();
			if (null != createdRelations) {
				for (Relationship rel : createdRelations) {
					if (rel.getStartNode().hasProperty(SystemProperties.IL_SYS_NODE_TYPE.name())
							&& StringUtils.equalsIgnoreCase(
									rel.getStartNode().getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(),
									GraphDACParams.TAG.name())) {
						if (rel.getStartNode().hasProperty(SystemProperties.IL_TAG_NAME.name())) {
							Map<String, Object> transactionData = new HashMap<String, Object>();
							List<String> tags = new ArrayList<String>();
							transactionData.put(GraphDACParams.properties.name(), new HashMap<String, Object>());
							transactionData.put(GraphDACParams.addedTags.name(), new ArrayList<String>());
							tags.add(rel.getStartNode().getProperty(SystemProperties.IL_TAG_NAME.name()).toString());
							transactionData.put(GraphDACParams.removedTags.name(), tags);

							Map<String, Object> map = setMessageData(graphDb, rel.getEndNode().getId(), userId,
									requestId, GraphDACParams.UPDATE.name(), transactionData);
							lstMessageMap.add(map);
						}
					}
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error building removed tags message" + e.getMessage(), e);
		}
		return lstMessageMap;
	}

	private List<Map<String, Object>> getAddedRelationShipMessages(TransactionData data, String userId,
			String requestId) {
		Iterable<Relationship> createdRelations = data.createdRelationships();
		return getRelationShipMessages(createdRelations, GraphDACParams.UPDATE.name(), false, userId, requestId, null);
	}

	private List<Map<String, Object>> getRemovedRelationShipMessages(TransactionData data, String userId,
			String requestId) {
		Iterable<Relationship> deletedRelations = data.deletedRelationships();
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Relationship>> removedRelationshipProp = data
				.removedRelationshipProperties();
		return getRelationShipMessages(deletedRelations, GraphDACParams.UPDATE.name(), true, userId, requestId,
				removedRelationshipProp);
	}

	private List<Map<String, Object>> getRelationShipMessages(Iterable<Relationship> relations, String operationType,
			boolean delete, String userId, String requestId,
			Iterable<org.neo4j.graphdb.event.PropertyEntry<Relationship>> removedRelationshipProp) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		try {
			if (null != relations) {
				for (Relationship rel : relations) {
					Node startNode = rel.getStartNode();
					Node endNode = rel.getEndNode();
					Map<String, Object> relMetadata = null;
					if (delete)
						relMetadata = getRelationShipPropertyEntry(rel.getId(), removedRelationshipProp);
					else
						relMetadata = rel.getAllProperties();
					String relationTypeName = rel.getType().name();
					if (StringUtils.equalsIgnoreCase(
							startNode.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(),
							GraphDACParams.TAG.name()))
						continue;

					// start_node message
					Map<String, Object> map = null;
					Map<String, Object> transactionData = new HashMap<String, Object>();
					Map<String, Object> startRelation = new HashMap<>();

					startRelation.put("rel", relationTypeName);
					startRelation.put("id", endNode.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
					startRelation.put("dir", "OUT");
					if (endNode.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
						startRelation.put("type", endNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
					startRelation.put("label", getLabel(endNode));
					startRelation.put("relMetadata", relMetadata);

					if (StringUtils.isEmpty(userId)) {
						String startNodeLastUpdate = (String) getPropertyValue(startNode, "lastUpdatedOn");
						String endNodeLastUpdate = (String) getPropertyValue(endNode, "lastUpdatedOn");

						if (startNodeLastUpdate != null && endNodeLastUpdate != null) {
							if (startNodeLastUpdate.compareTo(endNodeLastUpdate) > 0) {
								userId = (String) getPropertyValue(startNode, "lastUpdatedBy");
							} else {
								userId = (String) getPropertyValue(endNode, "lastUpdatedBy");
							}
						}
						if (StringUtils.isBlank(userId))
							userId = "ANONYMOUS";
					}
					List<Map<String, Object>> startRelations = new ArrayList<Map<String, Object>>();
					startRelations.add(startRelation);
					transactionData.put(GraphDACParams.properties.name(), new HashMap<String, Object>());
					transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
					transactionData.put(GraphDACParams.addedTags.name(), new ArrayList<String>());
					if (delete) {
						transactionData.put(GraphDACParams.removedRelations.name(), startRelations);
						transactionData.put(GraphDACParams.addedRelations.name(), new ArrayList<Map<String, Object>>());
					} else {
						transactionData.put(GraphDACParams.addedRelations.name(), startRelations);
						transactionData.put(GraphDACParams.removedRelations.name(),
								new ArrayList<Map<String, Object>>());
					}

					map = setMessageData(graphDb, startNode.getId(), userId, requestId, operationType, transactionData);
					lstMessageMap.add(map);

					// end_node message
					map = null;
					transactionData = new HashMap<String, Object>();
					Map<String, Object> endRelation = new HashMap<>();

					endRelation.put("rel", relationTypeName);
					endRelation.put("id", startNode.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
					endRelation.put("dir", "IN");
					if (startNode.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
						endRelation.put("type", startNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
					endRelation.put("label", getLabel(startNode));
					endRelation.put("relMetadata", relMetadata);
					List<Map<String, Object>> endRelations = new ArrayList<Map<String, Object>>();
					endRelations.add(endRelation);
					transactionData.put(GraphDACParams.properties.name(), new HashMap<String, Object>());
					transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
					transactionData.put(GraphDACParams.addedTags.name(), new ArrayList<String>());
					if (delete) {
						transactionData.put(GraphDACParams.removedRelations.name(), endRelations);
						transactionData.put(GraphDACParams.addedRelations.name(), new ArrayList<Map<String, Object>>());
					} else {
						transactionData.put(GraphDACParams.addedRelations.name(), endRelations);
						transactionData.put(GraphDACParams.removedRelations.name(),
								new ArrayList<Map<String, Object>>());
					}

					map = setMessageData(graphDb, endNode.getId(), userId, requestId, operationType, transactionData);
					lstMessageMap.add(map);
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error building updated relations message" + e.getMessage(), e);
		}
		return lstMessageMap;
	}

	private Map<String, Object> getRelationShipPropertyEntry(Long relId,
			Iterable<org.neo4j.graphdb.event.PropertyEntry<Relationship>> relProp) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (org.neo4j.graphdb.event.PropertyEntry<Relationship> pe : relProp) {
			if (relId == pe.entity().getId()) {
				if (pe.previouslyCommitedValue() != null) {
					map.put((String) pe.key(), pe.previouslyCommitedValue());
				}
			}
		}
		return map;
	}

	private String getLabel(Node node) {
		if (node.hasProperty("name")) {
			return (String) node.getProperty("name");
		} else if (node.hasProperty("lemma")) {
			return (String) node.getProperty("lemma");
		} else if (node.hasProperty("title")) {
			return (String) node.getProperty("title");
		} else if (node.hasProperty("gloss")) {
			return (String) node.getProperty("gloss");
		}
		return "";
	}

	@SuppressWarnings("rawtypes")
	private String getLabel(Map<String, Object> nodeMap) {
		if (nodeMap.containsKey("name")) {
			return (String) ((Map) nodeMap.get("name")).get("ov");
		} else if (nodeMap.containsKey("lemma")) {
			return (String) ((Map) nodeMap.get("lemma")).get("ov");
		} else if (nodeMap.containsKey("title")) {
			return (String) ((Map) nodeMap.get("title")).get("ov");
		} else if (nodeMap.containsKey("gloss")) {
			return (String) ((Map) nodeMap.get("gloss")).get("ov");
		}

		return "";
	}

	private Object getPropertyValue(Node node, String propertyName) {
		if (node.hasProperty(propertyName))
			return node.getProperty(propertyName);
		return null;
	}

	private List<Long> getUpdatedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		List<Long> lstCreatedNodeIds = getCreatedNodeIds(data);
		List<Long> lstDeletedNodeIds = getDeletedNodeIds(data);
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe : assignedNodeProp) {
			if (!lstCreatedNodeIds.contains(pe.entity().getId()) && !lstDeletedNodeIds.contains(pe.entity().getId())) {
				lstNodeIds.add(pe.entity().getId());
			}
		}
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.removedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe : removedNodeProp) {
			if (!lstCreatedNodeIds.contains(pe.entity().getId()) && !lstDeletedNodeIds.contains(pe.entity().getId())) {
				lstNodeIds.add(pe.entity().getId());
			}
		}
		return new ArrayList<Long>(new HashSet<Long>(lstNodeIds));
	}

	private List<Long> getCreatedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		if (null != data.createdNodes()) {
			Iterator<Node> nodes = data.createdNodes().iterator();
			while (nodes.hasNext()) {
				lstNodeIds.add(nodes.next().getId());
			}
		}

		return new ArrayList<Long>(new HashSet<Long>(lstNodeIds));
	}

	private List<Long> getDeletedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		if (null != data.deletedNodes()) {
			Iterator<Node> nodes = data.deletedNodes().iterator();
			while (nodes.hasNext()) {
				lstNodeIds.add(nodes.next().getId());
			}
		}

		return new ArrayList<Long>(new HashSet<Long>(lstNodeIds));
	}

	private Map<String, Object> setMessageData(GraphDatabaseService graphDb, Long nodeId, String userId,
			String requestId, String operationType, Map<String, Object> transactionData) {
		Map<String, Object> map = new HashMap<String, Object>();
		Node node = graphDb.getNodeById(nodeId);
		map.put(GraphDACParams.requestId.name(), requestId);
		if (StringUtils.isEmpty(userId)) {
			if (node.hasProperty("lastUpdatedBy"))
				userId = (String) node.getProperty("lastUpdatedBy");
			else if(node.hasProperty("createdBy"))
				userId = (String) node.getProperty("createdBy");
			else
				userId = "ANONYMOUS";
		}
		String channelId = null;
		if (node.hasProperty(GraphDACParams.channel.name())) {
			channelId = node.getProperty(GraphDACParams.channel.name()).toString();
		}
		map.put(GraphDACParams.userId.name(), userId);
		map.put(GraphDACParams.operationType.name(), operationType);
		map.put(GraphDACParams.label.name(), getLabel(node));
		map.put(GraphDACParams.createdOn.name(), DateUtils.format(new Date()));
		map.put(GraphDACParams.ets.name(), System.currentTimeMillis());
		map.put(GraphDACParams.graphId.name(), getGraphId(node));
		map.put(GraphDACParams.nodeGraphId.name(), nodeId);
		map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
		if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
			map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
		if (node.hasProperty(SystemProperties.IL_SYS_NODE_TYPE.name()))
			map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
		map.put(GraphDACParams.channel.name(), channelId);
		map.put(GraphDACParams.transactionData.name(), transactionData);
		map.put(GraphDACParams.mid.name(), getUUID());
		return map;

	}

	private String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}
}