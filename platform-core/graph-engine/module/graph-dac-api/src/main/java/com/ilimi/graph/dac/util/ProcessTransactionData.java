package com.ilimi.graph.dac.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;

public class ProcessTransactionData {
	
	protected String graphId;
	protected GraphDatabaseService graphDb;

	public ProcessTransactionData(String graphId, GraphDatabaseService graphDb) {
		this.graphId = graphId;
		this.graphDb = graphDb;
	}
	
	public void processTxnData (TransactionData data) {
		System.out.println("Txn Data : " + data.toString());
		getMessageObj(data);
	}
	
	private  String getGraphId() {
		return this.graphId;
	}

	private  List<Map<String, Object>> getMessageObj (TransactionData data) {
		List<Map<String, Object>> messageMap = new ArrayList<Map<String, Object>>();
		messageMap.addAll(getCretedNodeMessages(data, graphDb));
		messageMap.addAll(getUpdatedNodeMessages(data, graphDb));
		messageMap.addAll(getDeletedNodeMessages(data, graphDb));
		messageMap.addAll(getRetiredNodeMessages(data, graphDb));
		
		return messageMap;
	}

	private List<Map<String, Object>> getCretedNodeMessages(TransactionData data, GraphDatabaseService graphDb) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		List<Long> createdNodeIds = getCreatedNodeIds(data);
		for (Long nodeId: createdNodeIds) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> transactionData = new HashMap<String, Object>();
			transactionData.put(GraphDACParams.addedProperties.name(), getAssiNodePropEntry(nodeId, data));
			transactionData.put(GraphDACParams.removedProperties.name(), new ArrayList<String>());
			transactionData.put(GraphDACParams.adedTags.name(), new ArrayList<String>());
			transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
			Node node = graphDb.getNodeById(nodeId);
			map.put(GraphDACParams.operationType.name(), GraphDACParams.CREATE.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), node.getProperty(GraphDACParams.identifier.name()));
			map.put(GraphDACParams.nodeUniqueId.name(), nodeId);
			map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
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
			transactionData.put(GraphDACParams.addedProperties.name(), getAssiNodePropEntry(nodeId, data));
			transactionData.put(GraphDACParams.removedProperties.name(), getRemoNodePropEntry(nodeId, data));
			transactionData.put(GraphDACParams.adedTags.name(), new ArrayList<String>());
			transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
			Node node = graphDb.getNodeById(nodeId);
			map.put(GraphDACParams.operationType.name(), GraphDACParams.UPDATE.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), node.getProperty(GraphDACParams.identifier.name()));
			map.put(GraphDACParams.nodeUniqueId.name(), nodeId);
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
			transactionData.put(GraphDACParams.addedProperties.name(), new HashMap<String, Object>());
			transactionData.put(GraphDACParams.removedProperties.name(), getRemoNodePropEntry(nodeId, data));
			transactionData.put(GraphDACParams.adedTags.name(), new ArrayList<String>());
			transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
			Node node = graphDb.getNodeById(nodeId);				// Assuming that the handler will be hooked in 'beforeCommit' event
			map.put(GraphDACParams.operationType.name(), GraphDACParams.DELETE.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), node.getProperty(GraphDACParams.identifier.name()));
			map.put(GraphDACParams.nodeUniqueId.name(), nodeId);
			map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}

		return lstMessageMap;
	}
	
	private List<Map<String, Object>> getRetiredNodeMessages(TransactionData data, GraphDatabaseService graphDb) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
		List<Long> retiredNodeIds = getRetiredNodeIds(data);
		for (Long nodeId: retiredNodeIds) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> transactionData = new HashMap<String, Object>();
			transactionData.put(GraphDACParams.addedProperties.name(), getAssiNodePropEntry(nodeId, data));
			transactionData.put(GraphDACParams.removedProperties.name(), new HashMap<String, Object>());
			transactionData.put(GraphDACParams.adedTags.name(), new ArrayList<String>());
			transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
			Node node = graphDb.getNodeById(nodeId);				// Assuming that the handler will be hooked in 'beforeCommit' event
			map.put(GraphDACParams.operationType.name(), GraphDACParams.RETIRED.name());
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), node.getProperty(GraphDACParams.identifier.name()));
			map.put(GraphDACParams.nodeUniqueId.name(), nodeId);
			map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}

		return lstMessageMap;
	}
	
	private Map<String, Object> getAssiNodePropEntry(Long nodeId, TransactionData data) {
		Map<String, Object> map = new HashMap<String, Object>();
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: assignedNodeProp) {
			if (nodeId == pe.entity().getId()) {
				System.out.println("Key : " + pe.key());
				System.out.println("New Value : " + pe.value());
				System.out.println("Old Value : " + pe.previouslyCommitedValue());
				map.put((String) pe.key(), pe.value());
			}
		}
		return map;
	}
	
	private List<String> getRemoNodePropEntry(Long nodeId, TransactionData data) {
		List<String> lst = new ArrayList<String>();
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: removedNodeProp) {
			if (nodeId == pe.entity().getId()) {
				System.out.println("Key : " + pe.key());
				System.out.println("New Value : " + pe.value());
				System.out.println("Old Value : " + pe.previouslyCommitedValue());
				lst.add((String) pe.key());
			}
		}
		return lst;
	}
	
	private List<Long> getRetiredNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: assignedNodeProp) {
			if (StringUtils.equalsIgnoreCase(GraphDACParams.status.name(), (CharSequence) pe.key()) && 
					StringUtils.equalsIgnoreCase(GraphDACParams.RETIRED.name(), (CharSequence) pe.value())) {
				System.out.println("Key : " + pe.key());
				System.out.println("New Value : " + pe.value());
				System.out.println("Old Value : " + pe.previouslyCommitedValue());
				lstNodeIds.add(pe.entity().getId());
			}
		}
		return lstNodeIds;
	}
	
	private List<Long> getUpdatedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		List<Long> lstCreatedNodeIds = getCreatedNodeIds(data);
		List<Long> lstRetiredNodeIds = getRetiredNodeIds(data);
		List<Long> lstDeletedNodeIds = getDeletedNodeIds(data);
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: assignedNodeProp) {
			if (!lstCreatedNodeIds.contains(pe.entity().getId()) &&
					!lstRetiredNodeIds.contains(pe.entity().getId()) && 
					! lstDeletedNodeIds.contains(pe.entity().getId())) {
				System.out.println("Key : " + pe.key());
				System.out.println("New Value : " + pe.value());
				System.out.println("Old Value : " + pe.previouslyCommitedValue());
				lstNodeIds.add(pe.entity().getId());
			}
		}
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.removedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: removedNodeProp) {
			if (!lstCreatedNodeIds.contains(pe.entity().getId()) &&
					!lstRetiredNodeIds.contains(pe.entity().getId()) && 
					! lstDeletedNodeIds.contains(pe.entity().getId())) {
				System.out.println("Key : " + pe.key());
				System.out.println("New Value : " + pe.value());
				System.out.println("Old Value : " + pe.previouslyCommitedValue());
				lstNodeIds.add(pe.entity().getId());
			}
		}
		return lstNodeIds;
	}

	private List<Long> getCreatedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		if (null != data.createdNodes()) {
            Iterator<Node> nodes =  data.createdNodes().iterator();
            while (nodes.hasNext()) {
            	lstNodeIds.add(nodes.next().getId());
            }
        }
		
		return lstNodeIds;
	}

	private List<Long> getDeletedNodeIds(TransactionData data) {
		List<Long> lstNodeIds = new ArrayList<Long>();
		if (null != data.createdNodes()) {
            Iterator<Node> nodes =  data.deletedNodes().iterator();
            while (nodes.hasNext()) {
            	lstNodeIds.add(nodes.next().getId());
            }
        }
		
		return lstNodeIds;
	}
}
