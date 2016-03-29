package com.ilimi.graph.dac.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.searchindex.producer.KafkaMessageProducer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.TransactionData;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;

public class ProcessTransactionData {
	
	private static Logger LOGGER = LogManager.getLogger(ProcessTransactionData.class.getName());
	
	protected String graphId;
	protected GraphDatabaseService graphDb;

	public ProcessTransactionData(String graphId, GraphDatabaseService graphDb) {
		this.graphId = graphId;
		this.graphDb = graphDb;
	}
	
	public void processTxnData (TransactionData data) {
		LOGGER.debug("Txn Data : " + data.toString());
		pushMessageToKafka(getMessageObj(data));
	}
	
	private  String getGraphId() {
		return this.graphId;
	}

	private  List<Map<String, Object>> getMessageObj (TransactionData data) {
		List<Map<String, Object>> messageMap = new ArrayList<Map<String, Object>>();
		messageMap.addAll(getCretedNodeMessages(data, graphDb));
		messageMap.addAll(getUpdatedNodeMessages(data, graphDb));
		messageMap.addAll(getDeletedNodeMessages(data, graphDb));
		
		return messageMap;
	}
	
	private void pushMessageToKafka(List<Map<String, Object>> messages) {
		if (messages.size() <= 0) return; 
		LOGGER.debug("Sending to KAFKA.... ");
		KafkaMessageProducer producer = new KafkaMessageProducer();
		producer.init();
		for (Map<String, Object> message: messages) {
			producer.pushMessage(message);
		}
		LOGGER.debug("Sending to KAFKA : FINISHED");
	}

	private List<Map<String, Object>> getCretedNodeMessages(TransactionData data, GraphDatabaseService graphDb) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		List<Long> createdNodeIds = getCreatedNodeIds(data);
		for (Long nodeId: createdNodeIds) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> transactionData = new HashMap<String, Object>();
			transactionData.put(GraphDACParams.addedProperties.name(), getAssignedNodePropertyEntry(nodeId, data));
			transactionData.put(GraphDACParams.removedProperties.name(), new ArrayList<String>());
			transactionData.put(GraphDACParams.addedTags.name(), getAddedTagsName(nodeId, data));
			transactionData.put(GraphDACParams.removedTags.name(), getRemovedTagsName(nodeId, data));
			Node node = graphDb.getNodeById(nodeId);
			map.put(GraphDACParams.operationType.name(), GraphDACParams.CREATE.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), nodeId);
			map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
			    map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			if (node.hasProperty(SystemProperties.IL_SYS_NODE_TYPE.name()))
			    map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}

		return lstMessageMap;
	}
	
	private List<Map<String, Object>> getUpdatedNodeMessages(TransactionData data, GraphDatabaseService graphDb) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		List<Long> updatedNodeIds = getUpdatedNodeIds(data);
		for (Long nodeId: updatedNodeIds) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> transactionData = new HashMap<String, Object>();
			transactionData.put(GraphDACParams.addedProperties.name(), getAssignedNodePropertyEntry(nodeId, data));
			transactionData.put(GraphDACParams.removedProperties.name(), new ArrayList<String>(getRemovedNodePropertyEntry(nodeId, data).keySet()));
			transactionData.put(GraphDACParams.addedTags.name(), getAddedTagsName(nodeId, data));
			transactionData.put(GraphDACParams.removedTags.name(), getRemovedTagsName(nodeId, data));
			Node node = graphDb.getNodeById(nodeId);
			map.put(GraphDACParams.operationType.name(), GraphDACParams.UPDATE.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), nodeId);
			map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}

		return lstMessageMap;
	}
	
	private List<Map<String, Object>> getDeletedNodeMessages(TransactionData data, GraphDatabaseService graphDb) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		List<Long> deletedNodeIds = getDeletedNodeIds(data);
		for (Long nodeId: deletedNodeIds) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> transactionData = new HashMap<String, Object>();
			Map<String, Object> removedNodeProp = getRemovedNodePropertyEntry(nodeId, data);
			transactionData.put(GraphDACParams.addedProperties.name(), new HashMap<String, Object>());
			transactionData.put(GraphDACParams.removedProperties.name(), new ArrayList<String>(removedNodeProp.keySet()));
			transactionData.put(GraphDACParams.addedTags.name(), getAddedTagsName(nodeId, data));
			transactionData.put(GraphDACParams.removedTags.name(), getRemovedTagsName(nodeId, data));
			map.put(GraphDACParams.operationType.name(), GraphDACParams.DELETE.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), nodeId);
			map.put(GraphDACParams.nodeUniqueId.name(), removedNodeProp.get(SystemProperties.IL_UNIQUE_ID.name()));
			map.put(GraphDACParams.objectType.name(), removedNodeProp.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			map.put(GraphDACParams.nodeType.name(), removedNodeProp.get(SystemProperties.IL_SYS_NODE_TYPE.name()));
			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}

		return lstMessageMap;
	}
	
	@SuppressWarnings("unused")
	private List<Map<String, Object>> getRetiredNodeMessages(TransactionData data, GraphDatabaseService graphDb) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		List<Long> retiredNodeIds = getRetiredNodeIds(data);
		for (Long nodeId: retiredNodeIds) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> transactionData = new HashMap<String, Object>();
			transactionData.put(GraphDACParams.addedProperties.name(), getAssignedNodePropertyEntry(nodeId, data));
			transactionData.put(GraphDACParams.removedProperties.name(), new HashMap<String, Object>());
			transactionData.put(GraphDACParams.addedTags.name(), getAddedTagsName(nodeId, data));
			transactionData.put(GraphDACParams.removedTags.name(), getRemovedTagsName(nodeId, data));
			Node node = graphDb.getNodeById(nodeId);				// Assuming that the handler will be hooked in 'beforeCommit' event
			map.put(GraphDACParams.operationType.name(), GraphDACParams.RETIRED.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), nodeId);
			map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}

		return lstMessageMap;
	}
	
	private Map<String, Object> getAssignedNodePropertyEntry(Long nodeId, TransactionData data) {
		Map<String, Object> map = new HashMap<String, Object>();
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: assignedNodeProp) {
			if (nodeId == pe.entity().getId()) {
				LOGGER.debug("Key : " + pe.key());
				LOGGER.debug("New Value : " + pe.value());
				LOGGER.debug("Old Value : " + pe.previouslyCommitedValue());
				map.put((String) pe.key(), pe.value());
			}
		}
		return map;
	}
	
	private List<String> getAddedTagsName(Long nodeId, TransactionData data) {
		List<String> tags = new ArrayList<String>();
		Iterable<Relationship> createdRelations = data.createdRelationships();
		for (Relationship rel: createdRelations) {
			if (nodeId == rel.getEndNode().getId() && 
				StringUtils.equalsIgnoreCase(
						rel.getStartNode().getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(), 
						GraphDACParams.TAG.name())) {
				tags.add(rel.getStartNode().getProperty(SystemProperties.IL_TAG_NAME.name()).toString());
			}
		}
		return tags;
	}
	
	private Map<String, Object> getRemovedNodePropertyEntry(Long nodeId, TransactionData data) {
		Map<String, Object> map = new HashMap<String, Object>();
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.removedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: removedNodeProp) {
			if (nodeId == pe.entity().getId()) {
				LOGGER.debug("Key : " + pe.key());
				LOGGER.debug("Old Value : " + pe.previouslyCommitedValue());
				map.put((String) pe.key(), pe.previouslyCommitedValue());
			}
		}
		return map;
	}
	
	private List<String> getRemovedTagsName(Long nodeId, TransactionData data) {
		List<String> tags = new ArrayList<String>();
		Iterable<Relationship> deletedRelations = data.deletedRelationships();
		for (Relationship rel: deletedRelations) {
			if (nodeId == rel.getEndNode().getId() && 
				StringUtils.equalsIgnoreCase(
						rel.getStartNode().getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(), 
						GraphDACParams.TAG.name())) {
				tags.add(rel.getStartNode().getProperty(GraphDACParams.TAG_NAME.name()).toString());
			}
		}
		return tags;
	}
	
	private List<Long> getRetiredNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: assignedNodeProp) {
			if (StringUtils.equalsIgnoreCase(GraphDACParams.status.name(), (CharSequence) pe.key()) && 
					StringUtils.equalsIgnoreCase(GraphDACParams.RETIRED.name(), (CharSequence) pe.value())) {
				LOGGER.debug("Key : " + pe.key());
				LOGGER.debug("New Value : " + pe.value());
				LOGGER.debug("Old Value : " + pe.previouslyCommitedValue());
				lstNodeIds.add(pe.entity().getId());
			}
		}
		return new ArrayList<Long>(new HashSet<Long>(lstNodeIds));
	}
	
	private List<Long> getUpdatedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		List<Long> lstCreatedNodeIds = getCreatedNodeIds(data);
		List<Long> lstDeletedNodeIds = getDeletedNodeIds(data);
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: assignedNodeProp) {
			if (!lstCreatedNodeIds.contains(pe.entity().getId()) &&
					!lstDeletedNodeIds.contains(pe.entity().getId())) {
				LOGGER.debug("Key : " + pe.key());
				LOGGER.debug("New Value : " + pe.value());
				LOGGER.debug("Old Value : " + pe.previouslyCommitedValue());
				lstNodeIds.add(pe.entity().getId());
			}
		}
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.removedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: removedNodeProp) {
			if (!lstCreatedNodeIds.contains(pe.entity().getId()) &&
					!lstDeletedNodeIds.contains(pe.entity().getId())) {
				LOGGER.debug("Key : " + pe.key());
				LOGGER.debug("New Value : " + pe.value());
				LOGGER.debug("Old Value : " + pe.previouslyCommitedValue());
				lstNodeIds.add(pe.entity().getId());
			}
		}
		return new ArrayList<Long>(new HashSet<Long>(lstNodeIds));
	}

	private List<Long> getCreatedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		if (null != data.createdNodes()) {
            Iterator<Node> nodes =  data.createdNodes().iterator();
            while (nodes.hasNext()) {
            	lstNodeIds.add(nodes.next().getId());
            }
        }
		
		return new ArrayList<Long>(new HashSet<Long>(lstNodeIds));
	}

	private List<Long> getDeletedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		if (null != data.createdNodes()) {
            Iterator<Node> nodes =  data.deletedNodes().iterator();
            while (nodes.hasNext()) {
            	lstNodeIds.add(nodes.next().getId());
            }
        }
		
		return new ArrayList<Long>(new HashSet<Long>(lstNodeIds));
	}
}
