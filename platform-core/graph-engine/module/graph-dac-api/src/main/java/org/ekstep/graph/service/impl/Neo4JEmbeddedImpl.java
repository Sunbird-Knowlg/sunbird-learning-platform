package org.ekstep.graph.service.impl;

import java.util.HashMap;
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
	}

	@Override
	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		LOGGER.info("Calling 'createGraphUniqueContraint' Operation.");
		graphOperations.createGraphUniqueContraint(graphId, indexProperties, request);
	}

	@Override
	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		LOGGER.info("Calling 'createIndex' Operation.");
		graphOperations.createIndex(graphId, indexProperties, request);
	}

	@Override
	public void deleteGraph(String graphId, Request request) {
		LOGGER.info("Calling 'deleteGraph' Operation.");
		graphOperations.deleteGraph(graphId, request);
	}

	@Override
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.info("Calling 'createRelation' Operation.");
		graphOperations.createRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.info("Calling 'updateRelation' Operation.");
		graphOperations.updateRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.info("Calling 'deleteRelation' Operation.");
		graphOperations.deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.info("Calling 'createIncomingRelations' Operation.");
		graphOperations.createIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
	}

	@Override
	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.info("Calling 'createOutgoingRelations' Operation.");
		graphOperations.createOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
	}

	@Override
	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.info("Calling 'deleteIncomingRelations' Operation.");
		graphOperations.deleteIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
	}

	@Override
	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.info("Calling 'deleteOutgoingRelations' Operation.");
		graphOperations.deleteOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
	}

	@Override
	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		LOGGER.info("Calling 'removeRelationMetadataByKey' Operation.");
		graphOperations.removeRelationMetadataByKey(graphId, startNodeId, endNodeId, relationType, key, request);
	}

	@Override
	public void createCollection(String graphId, String collectionId, Node collection, String relationType,
			List<String> members, String indexProperty, Request request) {
		LOGGER.info("Calling 'createCollection' Operation.");
		graphOperations.createCollection(graphId, collectionId, collection, relationType, members, indexProperty,
				request);
	}

	@Override
	public void deleteCollection(String graphId, String collectionId, Request request) {
		LOGGER.info("Calling 'deleteCollection' Operation.");
		graphOperations.deleteCollection(graphId, collectionId, request);
	}

	@Override
	public Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request) {
		LOGGER.info("Calling 'importGraph' Operation.");
		Map<String, List<String>> messages = new HashMap<String, List<String>>();
		try {
			messages = graphOperations.importGraph(graphId, taskId, input, request);
		} catch (Exception e) {
			LOGGER.error("Error: Something went wrong while Importing the Graph.", e);
		}
		return messages;
	}

	@Override
	public Node upsertNode(String graphId, Node node, Request request) {
		LOGGER.info("Calling 'upsertNode' Operation.");
		return nodeOperations.upsertNode(graphId, node, request);
	}

	@Override
	public Node addNode(String graphId, Node node, Request request) {
		LOGGER.info("Calling 'addNode' Operation.");
		return nodeOperations.addNode(graphId, node, request);
	}

	@Override
	public Node updateNode(String graphId, Node node, Request request) {
		LOGGER.info("Calling 'updateNode' Operation.");
		return nodeOperations.updateNode(graphId, node, request);
	}

	@Override
	public void importNodes(String graphId, List<Node> nodes, Request request) {
		LOGGER.info("Calling 'importNodes' Operation.");
		nodeOperations.importNodes(graphId, nodes, request);
	}

	@Override
	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {
		LOGGER.info("Calling 'updatePropertyValue' Operation.");
		nodeOperations.updatePropertyValue(graphId, nodeId, property, request);
	}

	@Override
	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request) {
		LOGGER.info("Calling 'updatePropertyValues' Operation.");
		nodeOperations.updatePropertyValues(graphId, nodeId, metadata, request);
	}

	@Override
	public void removePropertyValue(String graphId, String nodeId, String key, Request request) {
		LOGGER.info("Calling 'removePropertyValue' Operation.");
		nodeOperations.removePropertyValue(graphId, nodeId, key, request);
	}

	@Override
	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {
		LOGGER.info("Calling 'removePropertyValues' Operation.");
		nodeOperations.removePropertyValues(graphId, nodeId, keys, request);
	}

	@Override
	public void deleteNode(String graphId, String nodeId, Request request) {
		LOGGER.info("Calling 'deleteNode' Operation.");
		nodeOperations.deleteNode(graphId, nodeId, request);
	}

	@Override
	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		LOGGER.info("Calling 'getNodeById' Operation.");
		return searchOperations.getNodeById(graphId, nodeId, getTags, request);
	}

	@Override
	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		LOGGER.info("Calling 'getNodeByUniqueId' Operation.");
		return searchOperations.getNodeByUniqueId(graphId, nodeId, getTags, request);
	}

	@Override
	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		LOGGER.info("Calling 'getNodesByProperty' Operation.");
		return searchOperations.getNodesByProperty(graphId, property, getTags, request);
	}

	@Override
	public List<Node> getNodesByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		LOGGER.info("Calling 'getNodesByUniqueIds' Operation.");
		return searchOperations.getNodeByUniqueIds(graphId, searchCriteria, request);
	}

	@Override
	public Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		LOGGER.info("Calling 'getNodeProperty' Operation.");
		return searchOperations.getNodeProperty(graphId, nodeId, key, request);
	}

	@Override
	public List<Node> getAllNodes(String graphId, Request request) {
		LOGGER.info("Calling 'getAllNodes' Operation.");
		return searchOperations.getAllNodes(graphId, request);
	}

	@Override
	public List<Relation> getAllRelations(String graphId, Request request) {
		LOGGER.info("Calling 'getAllRelations' Operation.");
		return searchOperations.getAllRelations(graphId, request);
	}

	@Override
	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		LOGGER.info("Calling 'getRelationProperty' Operation.");
		return searchOperations.getRelationProperty(graphId, startNodeId, relationType, endNodeId, key, request);
	}

	@Override
	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		LOGGER.info("Calling 'getRelation' Operation.");
		return searchOperations.getRelation(graphId, startNodeId, relationType, endNodeId, request);
	}

	@Override
	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		LOGGER.info("Calling 'checkCyclicLoop' Operation.");
		return searchOperations.checkCyclicLoop(graphId, startNodeId, relationType, endNodeId, request);
	}

	@Override
	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		LOGGER.info("Calling 'executeQuery' Operation.");
		return searchOperations.executeQuery(graphId, query, paramMap, request);
	}

	@Override
	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		LOGGER.info("Calling 'searchNodes' Operation.");
		return searchOperations.searchNodes(graphId, searchCriteria, getTags, request);
	}

	@Override
	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		LOGGER.info("Calling 'getNodesCount' Operation.");
		return searchOperations.getNodesCount(graphId, searchCriteria, request);
	}

	@Override
	public SubGraph traverse(String graphId, Traverser traverser, Request request) {
		LOGGER.info("Calling 'traverse' Operation.");
		return searchOperations.traverse(graphId, traverser, request);
	}

	@Override
	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		LOGGER.info("Calling 'traverseSubGraph' Operation.");
		return searchOperations.traverseSubGraph(graphId, traverser, request);
	}

	@Override
	public Graph getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request) {
		LOGGER.info("Calling 'getSubGraph' Operation.");
		return searchOperations.getSubGraph(graphId, startNodeId, relationType, depth, request);
	}

	@Override
	public org.neo4j.graphdb.Node upsertRootNode(String graphId, Request request) {
		LOGGER.info("Calling 'traverseSubGraph' Operation.");
		return nodeOperations.upsertRootNode(graphId, request);
	}

}
