package com.ilimi.graph.dac.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
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
	protected String userId;
	protected String requestId;
	protected GraphDatabaseService graphDb;

	public ProcessTransactionData(String graphId, String userId, String requestId, GraphDatabaseService graphDb) {
		this.graphId = graphId;
		this.graphDb = graphDb;
		this.userId = userId;
		this.requestId = requestId;
	}
	
	public void processTxnData (TransactionData data) {
		LOGGER.debug("Txn Data : " + data.toString());
		List<Map<String, Object>> kafkaMessages = getMessageObj(data);
		if(kafkaMessages != null && !kafkaMessages.isEmpty())
			pushMessageToKafka(kafkaMessages);
	}
	
	private  String getGraphId() {
		return this.graphId;
	}

	private  List<Map<String, Object>> getMessageObj (TransactionData data) {
		List<Map<String, Object>> messageMap = new ArrayList<Map<String, Object>>();
		messageMap.addAll(getCretedNodeMessages(data, graphDb));
		messageMap.addAll(getUpdatedNodeMessages(data, graphDb));
		messageMap.addAll(getDeletedNodeMessages(data, graphDb));
		messageMap.addAll(getAddedTagsMessage(data, graphDb));
		messageMap.addAll(getRemovedTagsMessage(data, graphDb));
		messageMap.addAll(getAddedRelationShipMessages(data));
		messageMap.addAll(getRemovedRelationShipMessages(data));

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
			//transactionData.put(GraphDACParams.addedProperties.name(), getAssignedNodePropertyEntry(nodeId, data));
			//transactionData.put(GraphDACParams.removedProperties.name(), new ArrayList<String>());
			transactionData.put(GraphDACParams.properties.name(),getAssignedNodePropertyEntry(nodeId, data));
			Node node = graphDb.getNodeById(nodeId);

			map.put(GraphDACParams.requestId.name(), requestId);
			if(StringUtils.isEmpty(userId)){
				if (node.hasProperty("lastUpdatedBy"))
					userId=(String) node.getProperty("lastUpdatedBy");
				else
					userId = "ANONYMOUS";
			}
			map.put(GraphDACParams.userId.name(), userId);
			map.put(GraphDACParams.operationType.name(), GraphDACParams.CREATE.name());
			map.put(GraphDACParams.label.name(), getLabel(node));
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
			//transactionData.put(GraphDACParams.addedProperties.name(), getAssignedNodePropertyEntry(nodeId, data));
			//transactionData.put(GraphDACParams.removedProperties.name(), new ArrayList<String>(getRemovedNodePropertyEntry(nodeId, data).keySet()));
			transactionData.put(GraphDACParams.properties.name(),getAllPropertyEntry(nodeId, data));

			Node node = graphDb.getNodeById(nodeId);
			map.put(GraphDACParams.requestId.name(), requestId);
			if(StringUtils.isEmpty(userId)){
				if (node.hasProperty("lastUpdatedBy"))
					userId=(String) node.getProperty("lastUpdatedBy");
				else
					userId = "ANONYMOUS";
			}
			map.put(GraphDACParams.userId.name(), userId);
			map.put(GraphDACParams.operationType.name(), GraphDACParams.UPDATE.name());
			map.put(GraphDACParams.label.name(), getLabel(node));
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), nodeId);
			map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
			if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
			    map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
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
			//transactionData.put(GraphDACParams.addedProperties.name(), new HashMap<String, Object>());
			//transactionData.put(GraphDACParams.removedProperties.name(), new ArrayList<String>(removedNodeProp.keySet()));
			transactionData.put(GraphDACParams.properties.name(),removedNodeProp);
			
			map.put(GraphDACParams.requestId.name(), requestId);
			if(StringUtils.isEmpty(userId)){
				if(removedNodeProp.containsKey("lastUpdatedBy"))
					userId=(String)((Map)removedNodeProp.get("lastUpdatedBy")).get("ov");//oldvalue of lastUpdatedBy from the transaction data as node is deleted
				else
					userId = "ANONYMOUS";
			}
			map.put(GraphDACParams.userId.name(), userId);
			map.put(GraphDACParams.operationType.name(), GraphDACParams.DELETE.name());
			map.put(GraphDACParams.label.name(), getLabel(removedNodeProp));
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), nodeId);
			map.put(GraphDACParams.nodeUniqueId.name(), ((Map)removedNodeProp.get(SystemProperties.IL_UNIQUE_ID.name())).get("ov"));
			map.put(GraphDACParams.objectType.name(), ((Map)removedNodeProp.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name())).get("ov"));
			map.put(GraphDACParams.nodeType.name(), ((Map)removedNodeProp.get(SystemProperties.IL_SYS_NODE_TYPE.name())).get("ov"));
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
			transactionData.put(GraphDACParams.properties.name(), getAssignedNodePropertyEntry(nodeId, data));
			transactionData.put(GraphDACParams.addedTags.name(), getAddedTagsName(nodeId, data));
			transactionData.put(GraphDACParams.removedTags.name(), getRemovedTagsName(nodeId, data));

			Node node = graphDb.getNodeById(nodeId);				// Assuming that the handler will be hooked in 'beforeCommit' event
			map.put(GraphDACParams.operationType.name(), GraphDACParams.RETIRED.name());
			map.put(GraphDACParams.label.name(), getLabel(node));
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), nodeId);
			map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
			    map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}

		return lstMessageMap;
	}
	
	
	private Map<String, Object> getAllPropertyEntry(Long nodeId, TransactionData data){
		Map<String, Object> map = getAssignedNodePropertyEntry(nodeId, data);
		map.putAll(getRemovedNodePropertyEntry(nodeId, data));
		return map;
	}
	
	private Map<String, Object> getAssignedNodePropertyEntry(Long nodeId, TransactionData data) {		
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> assignedNodeProp = data.assignedNodeProperties();
		return getNodePropertyEntry(nodeId, assignedNodeProp);
	}
	
	private Map<String, Object> getRemovedNodePropertyEntry(Long nodeId, TransactionData data) {
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.removedNodeProperties();
		return getNodePropertyEntry(nodeId, removedNodeProp);
	}
	
	private Map<String, Object> getNodePropertyEntry(Long nodeId, Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> nodeProp){
		Map<String, Object> map = new HashMap<String, Object>();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: nodeProp) {
			if (nodeId == pe.entity().getId()) {
				Map<String, Object> valueMap=new HashMap<String, Object>();
				valueMap.put("ov", pe.previouslyCommitedValue()); // old value
				valueMap.put("nv", pe.value()); // new value
				map.put((String) pe.key(), valueMap);
			}
		}
		return map;
	}
	
	private List<Map<String, Object>> getAddedTagsMessage(TransactionData data, GraphDatabaseService graphDb) {
        List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
        Iterable<Relationship> createdRelations = data.createdRelationships();
        for (Relationship rel: createdRelations) {
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
                    Node node = graphDb.getNodeById(rel.getEndNode().getId());
                    Map<String, Object> map = new HashMap<String, Object>();
                    if(StringUtils.isEmpty(userId)){
        				if (node.hasProperty("lastUpdatedBy"))
        					userId=(String) node.getProperty("lastUpdatedBy");
        				else
        					userId = "ANONYMOUS";
        			}
                    map.put(GraphDACParams.requestId.name(), requestId);
                	map.put(GraphDACParams.userId.name(), userId);
                    map.put(GraphDACParams.operationType.name(), GraphDACParams.CREATE.name());
                    map.put(GraphDACParams.label.name(), getLabel(node));
                    map.put(GraphDACParams.graphId.name(), getGraphId());
                    map.put(GraphDACParams.nodeGraphId.name(), node.getId());
                    map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
                    if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
                        map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
                    map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
                    map.put(GraphDACParams.transactionData.name(), transactionData);
                    lstMessageMap.add(map);
                }
            }
        }
        return lstMessageMap;
    }
	
	private List<Map<String, Object>> getRemovedTagsMessage(TransactionData data, GraphDatabaseService graphDb) {
        List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();
        Iterable<Relationship> createdRelations = data.deletedRelationships();
        for (Relationship rel: createdRelations) {
            if (StringUtils.equalsIgnoreCase(
                        rel.getStartNode().getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(), 
                        GraphDACParams.TAG.name())) {
                if (rel.getStartNode().hasProperty(SystemProperties.IL_TAG_NAME.name())) {
                    Map<String, Object> transactionData = new HashMap<String, Object>();
                    List<String> tags = new ArrayList<String>();
                    transactionData.put(GraphDACParams.properties.name(), new HashMap<String, Object>());
                    transactionData.put(GraphDACParams.addedTags.name(), new ArrayList<String>());
                    tags.add(rel.getStartNode().getProperty(SystemProperties.IL_TAG_NAME.name()).toString());
                    transactionData.put(GraphDACParams.removedTags.name(), tags);
                    Node node = graphDb.getNodeById(rel.getEndNode().getId());
                    Map<String, Object> map = new HashMap<String, Object>();
        			if(StringUtils.isEmpty(userId)){
        				if (node.hasProperty("lastUpdatedBy"))
        					userId=(String) node.getProperty("lastUpdatedBy");
        				else
        					userId = "ANONYMOUS";
        			}
                    map.put(GraphDACParams.requestId.name(), requestId);
                	map.put(GraphDACParams.userId.name(), userId);
                    map.put(GraphDACParams.operationType.name(), GraphDACParams.DELETE.name());
                    map.put(GraphDACParams.label.name(), getLabel(node));
                    map.put(GraphDACParams.graphId.name(), getGraphId());
                    map.put(GraphDACParams.nodeGraphId.name(), node.getId());
                    map.put(GraphDACParams.nodeUniqueId.name(), node.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
                    if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
                        map.put(GraphDACParams.objectType.name(), node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
                    map.put(GraphDACParams.nodeType.name(), node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));
                    map.put(GraphDACParams.transactionData.name(), transactionData);
                    lstMessageMap.add(map);
                }
            }
        }
        return lstMessageMap;
    }
	
	private List<Map<String, Object>> getAddedRelationShipMessages(TransactionData data) {
		Iterable<Relationship> createdRelations = data.createdRelationships();
		return getRelationShipMessages(createdRelations, GraphDACParams.CREATE.name());
	}
	
	private List<Map<String, Object>> getRemovedRelationShipMessages(TransactionData data) {
		Iterable<Relationship> deletedRelations = data.deletedRelationships();
		return getRelationShipMessages(deletedRelations, GraphDACParams.DELETE.name());
	}
	private List<Map<String, Object>> getRelationShipMessages(Iterable<Relationship> Relations, String operationType) {
		List<Map<String, Object>> lstMessageMap = new ArrayList<Map<String, Object>>();

		for (Relationship rel: Relations) {
			Node startNode = rel.getStartNode();
			Node endNode =  rel.getEndNode();
            String relationTypeName=rel.getType().name();
            
			if(StringUtils.equalsIgnoreCase(startNode.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(), 
					GraphDACParams.TAG.name()))
            	continue;

			//start_node message 
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> transactionData = new HashMap<String, Object>();
			Map<String, Object> startRelation = new HashMap<>();		

            startRelation.put("rel", relationTypeName);
            startRelation.put("id", endNode.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
            startRelation.put("dir", "OUT");
            startRelation.put("type", endNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
            startRelation.put("label", getLabel(endNode));
            
        	if(StringUtils.isEmpty(userId)){
            	String startNodeLastUpdate = (String) getPropertyValue(startNode , "lastUpdatedOn");
            	String endNodeLastUpdate = (String) getPropertyValue(endNode ,"lastUpdatedOn");
            	
            	if(startNodeLastUpdate != null && endNodeLastUpdate != null){
            		if(startNodeLastUpdate.compareTo(endNodeLastUpdate)>0){
            			userId=(String) getPropertyValue(startNode ,"lastUpdatedBy");
            		}else{
            			userId=(String) getPropertyValue(endNode ,"lastUpdatedBy");            			
            		}
            	}
            	
            	if(StringUtils.isEmpty(userId))
					userId = "ANONYMOUS";
        	}
        		
            transactionData.put(GraphDACParams.properties.name(), new HashMap<String, Object>());
            transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
            transactionData.put(GraphDACParams.addedTags.name(), new ArrayList<String>());
            transactionData.put(GraphDACParams.addedRelations.name(), startRelation);
            transactionData.put(GraphDACParams.removedRelations.name(), new HashMap<String, Object>());
            
        	map.put(GraphDACParams.requestId.name(), requestId);
        	map.put(GraphDACParams.userId.name(), userId);
			map.put(GraphDACParams.operationType.name(), operationType);
			map.put(GraphDACParams.label.name(), getLabel(startNode));
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), startNode.getId());
			map.put(GraphDACParams.nodeUniqueId.name(), startNode.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			map.put(GraphDACParams.nodeType.name(), startNode.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));			
			if (startNode.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
			    map.put(GraphDACParams.objectType.name(), startNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));

			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
			
			//end_node message 
			map = new HashMap<String, Object>();
			transactionData = new HashMap<String, Object>();
			Map<String, Object> endRelation = new HashMap<>();		

			endRelation.put("rel", relationTypeName);
			endRelation.put("id", startNode.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			endRelation.put("dir", "IN");
			endRelation.put("type", startNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));
			endRelation.put("label", getLabel(startNode));
            
            transactionData.put(GraphDACParams.properties.name(), new HashMap<String, Object>());
            transactionData.put(GraphDACParams.removedTags.name(), new ArrayList<String>());
            transactionData.put(GraphDACParams.addedTags.name(), new ArrayList<String>());
            transactionData.put(GraphDACParams.addedRelations.name(), endRelation);
            transactionData.put(GraphDACParams.removedRelations.name(), new HashMap<String, Object>());

            map.put(GraphDACParams.requestId.name(), requestId);            
			map.put(GraphDACParams.userId.name(), userId);
			map.put(GraphDACParams.operationType.name(), operationType);
			map.put(GraphDACParams.label.name(), getLabel(endNode));
			map.put(GraphDACParams.graphId.name(), getGraphId());
			map.put(GraphDACParams.nodeGraphId.name(), endNode.getId());
			map.put(GraphDACParams.nodeUniqueId.name(), endNode.getProperty(SystemProperties.IL_UNIQUE_ID.name()));
			map.put(GraphDACParams.nodeType.name(), endNode.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()));			
			if (startNode.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()))
			    map.put(GraphDACParams.objectType.name(), endNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name()));

			map.put(GraphDACParams.transactionData.name(), transactionData);
			lstMessageMap.add(map);
		}
        
		return lstMessageMap;
	}
	
	private String getLabel(Node node){
		
		if(node.hasProperty("name")){
			return (String) node.getProperty("name");
		}else if(node.hasProperty("lemma")){
			return (String) node.getProperty("lemma");
		}else if(node.hasProperty("title")){
			return (String) node.getProperty("title");
		}else if(node.hasProperty("gloss")){
			return (String) node.getProperty("gloss");
		}
		
		return "";
	}

	private String getLabel(Map<String, Object> nodeMap){
		
		if(nodeMap.containsKey("name")){
			return (String) ((Map)nodeMap.get("name")).get("ov");
		}else if(nodeMap.containsKey("lemma")){
			return (String) ((Map)nodeMap.get("lemma")).get("ov");
		}else if(nodeMap.containsKey("title")){
			return (String) ((Map)nodeMap.get("title")).get("ov");
		}else if(nodeMap.containsKey("gloss")){
			return (String) ((Map)nodeMap.get("gloss")).get("ov");
		}
		
		return "";
	}
	private Object getPropertyValue(Node node , String propertyName){
		if (node.hasProperty(propertyName))
			return node.getProperty(propertyName);
		return null;
	}
	
	private List<String> getAddedTagsName(Long nodeId, TransactionData data) {
		List<String> tags = new ArrayList<String>();
		Iterable<Relationship> createdRelations = data.createdRelationships();
		for (Relationship rel: createdRelations) {
			if (nodeId == rel.getEndNode().getId() && 
				StringUtils.equalsIgnoreCase(
						rel.getStartNode().getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(), 
						GraphDACParams.TAG.name())) {
			    if (rel.getStartNode().hasProperty(SystemProperties.IL_TAG_NAME.name()))
			        tags.add(rel.getStartNode().getProperty(SystemProperties.IL_TAG_NAME.name()).toString());
			}
		}
		return tags;
	}
	
	
	
	private List<String> getRemovedTagsName(Long nodeId, TransactionData data) {
		List<String> tags = new ArrayList<String>();
		Iterable<Relationship> deletedRelations = data.deletedRelationships();
		for (Relationship rel: deletedRelations) {
			if (nodeId == rel.getEndNode().getId() && 
				StringUtils.equalsIgnoreCase(
						rel.getStartNode().getProperty(SystemProperties.IL_SYS_NODE_TYPE.name()).toString(), 
						GraphDACParams.TAG.name())) {
			    if (rel.getStartNode().hasProperty(SystemProperties.IL_TAG_NAME.name()))
			        tags.add(rel.getStartNode().getProperty(SystemProperties.IL_TAG_NAME.name()).toString());
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
				lstNodeIds.add(pe.entity().getId());
			}
		}
		Iterable<org.neo4j.graphdb.event.PropertyEntry<Node>> removedNodeProp = data.removedNodeProperties();
		for (org.neo4j.graphdb.event.PropertyEntry<Node> pe: removedNodeProp) {
			if (!lstCreatedNodeIds.contains(pe.entity().getId()) &&
					!lstDeletedNodeIds.contains(pe.entity().getId())) {
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
