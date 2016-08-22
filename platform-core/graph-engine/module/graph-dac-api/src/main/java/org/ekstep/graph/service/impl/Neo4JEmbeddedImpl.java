package org.ekstep.graph.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.operation.Neo4JEmbeddedGraphOperations;
import org.ekstep.graph.service.operation.Neo4JEmbeddedNodeOperations;
import org.ekstep.graph.service.operation.Neo4JEmbeddedSearchOperations;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.importer.ImportData;

public class Neo4JEmbeddedImpl implements IGraphDatabaseService {

	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedImpl.class.getName());
	
	Neo4JEmbeddedGraphOperations graphOperations = new Neo4JEmbeddedGraphOperations();
	Neo4JEmbeddedNodeOperations nodeOperations = new Neo4JEmbeddedNodeOperations();
	Neo4JEmbeddedSearchOperations searchOperations = new Neo4JEmbeddedSearchOperations();

	@Override
	public void createGraph(String graphId, Request request) {
		LOGGER.info("Calling 'createGraph' Operation.");
		graphOperations.createGraph(graphId, request);
		LOGGER.info("Call to 'createGraph' Operation Finished.");		
	}

	@Override
	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		LOGGER.info("Calling 'createGraphUniqueContraint' Operation.");
		graphOperations.createGraphUniqueContraint(graphId, indexProperties, request);
		LOGGER.info("Call to 'createGraphUniqueContraint' Operation Finished.");		
	}

	@Override
	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		LOGGER.info("Calling 'createIndex' Operation.");
		graphOperations.createIndex(graphId, indexProperties, request);
		LOGGER.info("Call to 'createIndex' Operation Finished.");		
	}

	@Override
	public void deleteGraph(String graphId, Request request) {
		LOGGER.info("Calling 'deleteGraph' Operation.");
		graphOperations.deleteGraph(graphId, request);
		LOGGER.info("Call to 'deleteGraph' Operation Finished.");		
	}

	@Override
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.info("Calling 'createRelation' Operation.");
		graphOperations.createRelation(graphId, startNodeId, endNodeId, relationType, request);
		LOGGER.info("Call to 'createRelation' Operation Finished.");		
	}

	@Override
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.info("Calling 'updateRelation' Operation.");
		graphOperations.updateRelation(graphId, startNodeId, endNodeId, relationType, request);
		LOGGER.info("Call to 'updateRelation' Operation Finished.");		
	}

	@Override
	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.info("Calling 'deleteRelation' Operation.");
		graphOperations.deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
		LOGGER.info("Call to 'deleteRelation' Operation Finished.");		
	}

	@Override
	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.info("Calling 'createIncomingRelations' Operation.");
		graphOperations.createIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
		LOGGER.info("Call to 'createIncomingRelations' Operation Finished.");		
	}

	@Override
	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.info("Calling 'createOutgoingRelations' Operation.");
		graphOperations.createOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
		LOGGER.info("Call to 'createOutgoingRelations' Operation Finished.");		
	}

	@Override
	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.info("Calling 'deleteIncomingRelations' Operation.");
		graphOperations.deleteIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
		LOGGER.info("Call to 'deleteIncomingRelations' Operation Finished.");		
	}

	@Override
	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.info("Calling 'deleteOutgoingRelations' Operation.");
		LOGGER.info("Call to 'deleteOutgoingRelations' Operation Finished.");		
	}

	@Override
	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		LOGGER.info("Calling 'removeRelationMetadataByKey' Operation.");
		graphOperations.removeRelationMetadataByKey(graphId, startNodeId, endNodeId, relationType, key, request);
		LOGGER.info("Call to 'removeRelationMetadataByKey' Operation Finished.");		
	}

	@Override
	public void createCollection(String graphId, String collectionId, Node collection, String relationType,
			List<String> members, String indexProperty, Request request) {
		LOGGER.info("Calling 'createCollection' Operation.");
		graphOperations.createCollection(graphId, collectionId, collection, relationType, members, indexProperty, request);
		LOGGER.info("Call to 'createCollection' Operation Finished.");		
	}

	@Override
	public void deleteCollection(String graphId, String collectionId, Request request) {
		LOGGER.info("Calling 'deleteCollection' Operation.");
		graphOperations.deleteCollection(graphId, collectionId, request);
		LOGGER.info("Call to 'deleteCollection' Operation Finished.");		
	}

	@Override
	public void importGraph(String graphId, String taskId, ImportData input, Map<String, List<String>> messages, Request request) {
		LOGGER.info("Calling 'importGraph' Operation.");
		try {
			graphOperations.importGraph(graphId, taskId, input, messages, request);
		} catch (Exception e) {
			LOGGER.error("Error: Something went wrong while Importing the Graph.", e);
		}
		LOGGER.info("Call to 'importGraph' Operation Finished.");		
	}

	@Override
	public void upsertNode(String graphId, Node node, Request request) {
		LOGGER.info("Calling 'upsertNode' Operation.");
		nodeOperations.upsertNode(graphId, node, request);
		LOGGER.info("Call to 'upsertNode' Operation Finished.");		
	}

	@Override
	public void addNode(String graphId, Node node, Request request) {
		LOGGER.info("Calling 'addNode' Operation.");
		nodeOperations.addNode(graphId, node, request);
		LOGGER.info("Call to 'addNode' Operation Finished.");		
	}

	@Override
	public void updateNode(String graphId, Node node, Request request) {
		LOGGER.info("Calling 'updateNode' Operation.");
		nodeOperations.updateNode(graphId, node, request);
		LOGGER.info("Call to 'updateNode' Operation Finished.");		
	}

	@Override
	public void importNodes(String graphId, List<Node> nodes, Request request) {
		LOGGER.info("Calling 'importNodes' Operation.");
		nodeOperations.importNodes(graphId, nodes, request);
		LOGGER.info("Call to 'importNodes' Operation Finished.");		
	}

	@Override
	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {
		LOGGER.info("Calling 'updatePropertyValue' Operation.");
		nodeOperations.updatePropertyValue(graphId, nodeId, property, request);
		LOGGER.info("Call to 'updatePropertyValue' Operation Finished.");		
	}

	@Override
	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request) {
		LOGGER.info("Calling 'updatePropertyValues' Operation.");
		nodeOperations.updatePropertyValues(graphId, nodeId, metadata, request);
		LOGGER.info("Call to 'updatePropertyValues' Operation Finished.");
	}

	@Override
	public void removePropertyValue(String graphId, String nodeId, String key, Request request) {
		LOGGER.info("Calling 'removePropertyValue' Operation.");
		nodeOperations.removePropertyValue(graphId, nodeId, key, request);
		LOGGER.info("Call to 'removePropertyValue' Operation Finished.");		
	}

	@Override
	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {
		LOGGER.info("Calling 'removePropertyValues' Operation.");
		nodeOperations.removePropertyValues(graphId, nodeId, keys, request);
		LOGGER.info("Call to 'removePropertyValues' Operation Finished.");		
	}

	@Override
	public void deleteNode(String graphId, String nodeId, Request request) {
		LOGGER.info("Calling 'deleteNode' Operation.");
		nodeOperations.deleteNode(graphId, nodeId, request);
		LOGGER.info("Call to 'deleteNode' Operation Finished.");		
	}

	@Override
	public void getNodeById(String graphId, Long nodeId, Boolean getTags, Node node, Request request) {
		LOGGER.info("Calling 'getNodeById' Operation.");
		searchOperations.getNodeById(graphId, nodeId, getTags, node, request);
		LOGGER.info("Call to 'getNodeById' Operation Finished.");		
	}

	@Override
	public void getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Node node, Request request) {
		LOGGER.info("Calling 'getNodeByUniqueId' Operation.");
		searchOperations.getNodeByUniqueId(graphId, nodeId, getTags, node, request);
		LOGGER.info("Call to 'getNodeByUniqueId' Operation Finished.");		
	}

	@Override
	public void getNodesByProperty(String graphId, Property property, Boolean getTags, List<Node> nodeList, Request request) {
		LOGGER.info("Calling 'getNodesByProperty' Operation.");
		searchOperations.getNodesByProperty(graphId, property, getTags, nodeList, request);
		LOGGER.info("Call to 'getNodesByProperty' Operation Finished.");		
	}

	@Override
	public void getNodesByUniqueIds(String graphId, SearchCriteria searchCriteria, List<Node> nodes, Request request) {
		LOGGER.info("Calling 'getNodesByUniqueIds' Operation.");
		searchOperations.getNodeByUniqueIds(graphId, searchCriteria, nodes, request);
		LOGGER.info("Call to 'getNodesByUniqueIds' Operation Finished.");		
	}

	@Override
	public void getNodeProperty(String graphId, String nodeId, String key, Property property, Request request) {
		LOGGER.info("Calling 'getNodeProperty' Operation.");
		searchOperations.getNodeProperty(graphId, nodeId, key, property, request);
		LOGGER.info("Call to 'getNodeProperty' Operation Finished.");
	}

	@Override
	public void getAllNodes(String graphId, List<Node> nodes, Request request) {
		LOGGER.info("Calling 'getAllNodes' Operation.");
		searchOperations.getAllNodes(graphId, nodes, request);
		LOGGER.info("Call to 'getAllNodes' Operation Finished.");		
	}

	@Override
	public void getAllRelations(String graphId, List<Relation> relations, Request request) {
		LOGGER.info("Calling 'getAllRelations' Operation.");
		searchOperations.getAllRelations(graphId, relations, request);
		LOGGER.info("Call to 'getAllRelations' Operation Finished.");		
	}

	@Override
	public void getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Property property, Request request) {
		LOGGER.info("Calling 'getRelationProperty' Operation.");
		searchOperations.getRelationProperty(graphId, startNodeId, relationType, endNodeId, key, property, request);
		LOGGER.info("Call to 'getRelationProperty' Operation Finished.");		
	}

	@Override
	public void getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Relation relation, Request request) {
		LOGGER.info("Calling 'getRelation' Operation.");
		searchOperations.getRelation(graphId, startNodeId, relationType, endNodeId, relation, request);
		LOGGER.info("Call to 'getRelation' Operation Finished.");		
	}

	@Override
	public void checkCyclicLoop(String graphId, String startNodeId, String relationType, String endNodeId,
			Map<String, Object> vomap, Request request) {
		LOGGER.info("Calling 'checkCyclicLoop' Operation.");
		searchOperations.checkCyclicLoop(graphId, startNodeId, relationType, endNodeId, request);
		LOGGER.info("Call to 'checkCyclicLoop' Operation Finished.");		
	}

	@Override
	public void executeQuery(String graphId, String query, Map<String, Object> paramMap, List<Map<String, Object>> resultList, Request request) {
		LOGGER.info("Calling 'executeQuery' Operation.");
		searchOperations.executeQuery(graphId, query, paramMap, resultList, request);
		LOGGER.info("Call to 'executeQuery' Operation Finished.");		
	}

	@Override
	public void searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, List<Node> nodes, Request request) {
		LOGGER.info("Calling 'searchNodes' Operation.");
		searchOperations.searchNodes(graphId, searchCriteria, getTags, nodes, request);
		LOGGER.info("Call to 'searchNodes' Operation Finished.");		
	}

	@Override
	public void getNodesCount(String graphId, SearchCriteria searchCriteria, Long count, Request request) {
		LOGGER.info("Calling 'getNodesCount' Operation.");
		searchOperations.getNodesCount(graphId, searchCriteria, count, request);
		LOGGER.info("Call to 'getNodesCount' Operation Finished.");		
	}

	@Override
	public void traverse(String graphId, Traverser traverser, SubGraph subGraph, Request request) {
		LOGGER.info("Calling 'traverse' Operation.");
		searchOperations.traverse(graphId, traverser, subGraph, request);
		LOGGER.info("Call to 'traverse' Operation Finished.");		
	}

	@Override
	public void traverseSubGraph(String graphId, Traverser traverser, Graph subGraph, Request request) {
		LOGGER.info("Calling 'traverseSubGraph' Operation.");
		searchOperations.traverseSubGraph(graphId, traverser, subGraph, request);
		LOGGER.info("Call to 'traverseSubGraph' Operation Finished.");		
	}

	@Override
	public void getSubGraph(String graphId, String startNodeId, String relationType, int depth, Graph subGraph, Request request) {
		LOGGER.info("Calling 'getSubGraph' Operation.");
		searchOperations.getSubGraph(graphId, startNodeId, relationType, depth, subGraph, request);
		LOGGER.info("Call to 'getSubGraph' Operation Finished.");		
	}

}
