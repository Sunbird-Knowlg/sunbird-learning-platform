package org.ekstep.graph.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Property;
import org.ekstep.common.dto.Request;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.dac.model.Graph;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.dac.model.SubGraph;
import org.ekstep.graph.dac.model.Traverser;
import org.ekstep.graph.importer.ImportData;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.operation.Neo4JBoltGraphOperations;
import org.ekstep.graph.service.operation.Neo4JBoltNodeOperations;
import org.ekstep.graph.service.operation.Neo4JBoltSearchOperations;
import org.springframework.stereotype.Service;

@Service
public class Neo4JBoltImpl implements IGraphDatabaseService {

	Neo4JBoltGraphOperations graphOperations = new Neo4JBoltGraphOperations();
	Neo4JBoltNodeOperations nodeOperations = new Neo4JBoltNodeOperations();
	Neo4JBoltSearchOperations searchOperations = new Neo4JBoltSearchOperations();

	@Override
	public void createGraph(String graphId, Request request) {
		PlatformLogger.log("Calling 'createGraph' Operation.");
		graphOperations.createGraph(graphId, request);
	}

	@Override
	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		PlatformLogger.log("Calling 'createGraphUniqueContraint' Operation.");
		graphOperations.createGraphUniqueContraint(graphId, indexProperties, request);
	}

	@Override
	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		PlatformLogger.log("Calling 'createIndex' Operation.");
		graphOperations.createIndex(graphId, indexProperties, request);
	}

	@Override
	public void deleteGraph(String graphId, Request request) {
		PlatformLogger.log("Calling 'deleteGraph' Operation.");
		graphOperations.deleteGraph(graphId, request);
	}

	@Override
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		PlatformLogger.log("Calling 'createRelation' Operation.");
		graphOperations.createRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		PlatformLogger.log("Calling 'updateRelation' Operation.");
		graphOperations.updateRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		PlatformLogger.log("Calling 'deleteRelation' Operation.");
		graphOperations.deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		PlatformLogger.log("Calling 'createIncomingRelations' Operation.");
		graphOperations.createIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
	}

	@Override
	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		PlatformLogger.log("Calling 'createOutgoingRelations' Operation.");
		graphOperations.createOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
	}

	@Override
	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		PlatformLogger.log("Calling 'deleteIncomingRelations' Operation.");
		graphOperations.deleteIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
	}

	@Override
	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		PlatformLogger.log("Calling 'deleteOutgoingRelations' Operation.");
		graphOperations.deleteOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
	}

	@Override
	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		PlatformLogger.log("Calling 'removeRelationMetadataByKey' Operation.");
		graphOperations.removeRelationMetadataByKey(graphId, startNodeId, endNodeId, relationType, key, request);
	}

	@Override
	public void createCollection(String graphId, String collectionId, Node collection, String relationType,
			List<String> members, String indexProperty, Request request) {
		PlatformLogger.log("Calling 'createCollection' Operation.");
		graphOperations.createCollection(graphId, collectionId, collection, relationType, members, indexProperty,
				request);
	}

	@Override
	public void deleteCollection(String graphId, String collectionId, Request request) {
		PlatformLogger.log("Calling 'deleteCollection' Operation.");
		graphOperations.deleteCollection(graphId, collectionId, request);
	}

	@Override
	public Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request) {
		PlatformLogger.log("Calling 'importGraph' Operation.");
		Map<String, List<String>> messages = new HashMap<String, List<String>>();
		try {
			messages = graphOperations.importGraph(graphId, taskId, input, request);
		} catch (Exception e) {
			PlatformLogger.log("Error: Something went wrong while Importing the Graph.", e.getMessage(), e);
		}
		return messages;
	}

	@Override
	public Node upsertNode(String graphId, Node node, Request request) {
		PlatformLogger.log("Calling 'upsertNode' Operation.");
		return nodeOperations.upsertNode(graphId, node, request);
	}

	@Override
	public Node addNode(String graphId, Node node, Request request) {
		PlatformLogger.log("Calling 'addNode' Operation.");
		return nodeOperations.addNode(graphId, node, request);
	}

	@Override
	public Node updateNode(String graphId, Node node, Request request) {
		PlatformLogger.log("Calling 'updateNode' Operation.");
		return nodeOperations.updateNode(graphId, node, request);
	}

	@Override
	public void importNodes(String graphId, List<Node> nodes, Request request) {
		PlatformLogger.log("Calling 'importNodes' Operation.");
		nodeOperations.importNodes(graphId, nodes, request);
	}

	@Override
	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {
		PlatformLogger.log("Calling 'updatePropertyValue' Operation.");
		nodeOperations.updatePropertyValue(graphId, nodeId, property, request);
	}

	@Override
	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request) {
		PlatformLogger.log("Calling 'updatePropertyValues' Operation.");
		nodeOperations.updatePropertyValues(graphId, nodeId, metadata, request);
	}

	@Override
	public void removePropertyValue(String graphId, String nodeId, String key, Request request) {
		PlatformLogger.log("Calling 'removePropertyValue' Operation.");
		nodeOperations.removePropertyValue(graphId, nodeId, key, request);
	}

	@Override
	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {
		PlatformLogger.log("Calling 'removePropertyValues' Operation.");
		nodeOperations.removePropertyValues(graphId, nodeId, keys, request);
	}

	@Override
	public void deleteNode(String graphId, String nodeId, Request request) {
		PlatformLogger.log("Calling 'deleteNode' Operation.");
		nodeOperations.deleteNode(graphId, nodeId, request);
	}

	@Override
	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		PlatformLogger.log("Calling 'getNodeById' Operation.");
		return searchOperations.getNodeById(graphId, nodeId, getTags, request);
	}

	@Override
	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		PlatformLogger.log("Calling 'getNodeByUniqueId' Operation.");
		return searchOperations.getNodeByUniqueId(graphId, nodeId, getTags, request);
	}

	@Override
	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		PlatformLogger.log("Calling 'getNodesByProperty' Operation.");
		return searchOperations.getNodesByProperty(graphId, property, getTags, request);
	}

	@Override
	public List<Node> getNodesByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		PlatformLogger.log("Calling 'getNodesByUniqueIds' Operation.");
		return searchOperations.getNodeByUniqueIds(graphId, searchCriteria, request);
	}

	@Override
	public Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		PlatformLogger.log("Calling 'getNodeProperty' Operation.");
		return searchOperations.getNodeProperty(graphId, nodeId, key, request);
	}

	@Override
	public List<Node> getAllNodes(String graphId, Request request) {
		PlatformLogger.log("Calling 'getAllNodes' Operation.");
		return searchOperations.getAllNodes(graphId, request);
	}

	@Override
	public List<Relation> getAllRelations(String graphId, Request request) {
		PlatformLogger.log("Calling 'getAllRelations' Operation.");
		return searchOperations.getAllRelations(graphId, request);
	}

	@Override
	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		PlatformLogger.log("Calling 'getRelationProperty' Operation.");
		return searchOperations.getRelationProperty(graphId, startNodeId, relationType, endNodeId, key, request);
	}

	@Override
	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		PlatformLogger.log("Calling 'getRelation' Operation.");
		return searchOperations.getRelation(graphId, startNodeId, relationType, endNodeId, request);
	}

	@Override
	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		PlatformLogger.log("Calling 'checkCyclicLoop' Operation.");
		return searchOperations.checkCyclicLoop(graphId, startNodeId, relationType, endNodeId, request);
	}

	@Override
	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		PlatformLogger.log("Calling 'executeQuery' Operation.");
		return searchOperations.executeQuery(graphId, query, paramMap, request);
	}
	
	@Override
	public List<Map<String, Object>> executeQueryForProps(String graphId, String query, List<String> propKeys) {
		return searchOperations.executeQueryForProps(graphId, query, propKeys);
	}

	@Override
	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		PlatformLogger.log("Calling 'searchNodes' Operation.");
		return searchOperations.searchNodes(graphId, searchCriteria, getTags, request);
	}

	@Override
	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		PlatformLogger.log("Calling 'getNodesCount' Operation.");
		return searchOperations.getNodesCount(graphId, searchCriteria, request);
	}

	@Override
	public SubGraph traverse(String graphId, Traverser traverser, Request request) {
		PlatformLogger.log("Calling 'traverse' Operation.");
		return searchOperations.traverse(graphId, traverser, request);
	}

	@Override
	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		PlatformLogger.log("Calling 'traverseSubGraph' Operation.");
		return searchOperations.traverseSubGraph(graphId, traverser, request);
	}

	@Override
	public Graph getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request) {
		PlatformLogger.log("Calling 'getSubGraph' Operation.");
		return searchOperations.getSubGraph(graphId, startNodeId, relationType, depth, request);
	}

	@Override
	public Node upsertRootNode(String graphId, Request request) {
		PlatformLogger.log("Calling 'traverseSubGraph' Operation.");
		return nodeOperations.upsertRootNode(graphId, request);
	}

	@Override
	public void bulkUpdateNodes(String graphId, List<Map<String, Object>> newNodes,
			List<Map<String, Object>> modifiedNodes, List<Map<String, Object>> addOutRelations,
			List<Map<String, Object>> removeOutRelations, List<Map<String, Object>> addInRelations,
			List<Map<String, Object>> removeInRelations) {
		PlatformLogger.log("Calling 'traverseSubGraph' Operation.");
		graphOperations.bulkUpdateNodes(graphId, newNodes, modifiedNodes, addOutRelations, removeOutRelations,
				addInRelations, removeInRelations);
	}

}
