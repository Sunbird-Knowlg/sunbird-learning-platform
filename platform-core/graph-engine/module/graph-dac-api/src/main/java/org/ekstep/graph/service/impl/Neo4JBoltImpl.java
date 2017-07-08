package org.ekstep.graph.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.operation.Neo4JBoltGraphOperations;
import org.ekstep.graph.service.operation.Neo4JBoltNodeOperations;
import org.ekstep.graph.service.operation.Neo4JBoltSearchOperations;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.importer.ImportData;

public class Neo4JBoltImpl implements IGraphDatabaseService {

	private static ILogger LOGGER = PlatformLogManager.getLogger();
	
	Neo4JBoltGraphOperations graphOperations = new Neo4JBoltGraphOperations();
	Neo4JBoltNodeOperations nodeOperations = new Neo4JBoltNodeOperations();
	Neo4JBoltSearchOperations searchOperations = new Neo4JBoltSearchOperations();

	@Override
	public void createGraph(String graphId, Request request) {
		LOGGER.log("Calling 'createGraph' Operation.");
		graphOperations.createGraph(graphId, request);
	}

	@Override
	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		LOGGER.log("Calling 'createGraphUniqueContraint' Operation.");
		graphOperations.createGraphUniqueContraint(graphId, indexProperties, request);
	}

	@Override
	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		LOGGER.log("Calling 'createIndex' Operation.");
		graphOperations.createIndex(graphId, indexProperties, request);
	}

	@Override
	public void deleteGraph(String graphId, Request request) {
		LOGGER.log("Calling 'deleteGraph' Operation.");
		graphOperations.deleteGraph(graphId, request);
	}

	@Override
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.log("Calling 'createRelation' Operation.");
		graphOperations.createRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.log("Calling 'updateRelation' Operation.");
		graphOperations.updateRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.log("Calling 'deleteRelation' Operation.");
		graphOperations.deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

	@Override
	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.log("Calling 'createIncomingRelations' Operation.");
		graphOperations.createIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
	}

	@Override
	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.log("Calling 'createOutgoingRelations' Operation.");
		graphOperations.createOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
	}

	@Override
	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.log("Calling 'deleteIncomingRelations' Operation.");
		graphOperations.deleteIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
	}

	@Override
	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.log("Calling 'deleteOutgoingRelations' Operation.");
		graphOperations.deleteOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
	}

	@Override
	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		LOGGER.log("Calling 'removeRelationMetadataByKey' Operation.");
		graphOperations.removeRelationMetadataByKey(graphId, startNodeId, endNodeId, relationType, key, request);
	}

	@Override
	public void createCollection(String graphId, String collectionId, Node collection, String relationType,
			List<String> members, String indexProperty, Request request) {
		LOGGER.log("Calling 'createCollection' Operation.");
		graphOperations.createCollection(graphId, collectionId, collection, relationType, members, indexProperty,
				request);
	}

	@Override
	public void deleteCollection(String graphId, String collectionId, Request request) {
		LOGGER.log("Calling 'deleteCollection' Operation.");
		graphOperations.deleteCollection(graphId, collectionId, request);
	}

	@Override
	public Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request) {
		LOGGER.log("Calling 'importGraph' Operation.");
		Map<String, List<String>> messages = new HashMap<String, List<String>>();
		try {
			messages = graphOperations.importGraph(graphId, taskId, input, request);
		} catch (Exception e) {
			LOGGER.log("Error: Something went wrong while Importing the Graph.", e.getMessage(), e);
		}
		return messages;
	}

	@Override
	public Node upsertNode(String graphId, Node node, Request request) {
		LOGGER.log("Calling 'upsertNode' Operation.");
		return nodeOperations.upsertNode(graphId, node, request);
	}

	@Override
	public Node addNode(String graphId, Node node, Request request) {
		LOGGER.log("Calling 'addNode' Operation.");
		return nodeOperations.addNode(graphId, node, request);
	}

	@Override
	public Node updateNode(String graphId, Node node, Request request) {
		LOGGER.log("Calling 'updateNode' Operation.");
		return nodeOperations.updateNode(graphId, node, request);
	}

	@Override
	public void importNodes(String graphId, List<Node> nodes, Request request) {
		LOGGER.log("Calling 'importNodes' Operation.");
		nodeOperations.importNodes(graphId, nodes, request);
	}

	@Override
	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {
		LOGGER.log("Calling 'updatePropertyValue' Operation.");
		nodeOperations.updatePropertyValue(graphId, nodeId, property, request);
	}

	@Override
	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request) {
		LOGGER.log("Calling 'updatePropertyValues' Operation.");
		nodeOperations.updatePropertyValues(graphId, nodeId, metadata, request);
	}

	@Override
	public void removePropertyValue(String graphId, String nodeId, String key, Request request) {
		LOGGER.log("Calling 'removePropertyValue' Operation.");
		nodeOperations.removePropertyValue(graphId, nodeId, key, request);
	}

	@Override
	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {
		LOGGER.log("Calling 'removePropertyValues' Operation.");
		nodeOperations.removePropertyValues(graphId, nodeId, keys, request);
	}

	@Override
	public void deleteNode(String graphId, String nodeId, Request request) {
		LOGGER.log("Calling 'deleteNode' Operation.");
		nodeOperations.deleteNode(graphId, nodeId, request);
	}

	@Override
	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		LOGGER.log("Calling 'getNodeById' Operation.");
		return searchOperations.getNodeById(graphId, nodeId, getTags, request);
	}

	@Override
	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		LOGGER.log("Calling 'getNodeByUniqueId' Operation.");
		return searchOperations.getNodeByUniqueId(graphId, nodeId, getTags, request);
	}

	@Override
	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		LOGGER.log("Calling 'getNodesByProperty' Operation.");
		return searchOperations.getNodesByProperty(graphId, property, getTags, request);
	}

	@Override
	public List<Node> getNodesByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		LOGGER.log("Calling 'getNodesByUniqueIds' Operation.");
		return searchOperations.getNodeByUniqueIds(graphId, searchCriteria, request);
	}

	@Override
	public Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		LOGGER.log("Calling 'getNodeProperty' Operation.");
		return searchOperations.getNodeProperty(graphId, nodeId, key, request);
	}

	@Override
	public List<Node> getAllNodes(String graphId, Request request) {
		LOGGER.log("Calling 'getAllNodes' Operation.");
		return searchOperations.getAllNodes(graphId, request);
	}

	@Override
	public List<Relation> getAllRelations(String graphId, Request request) {
		LOGGER.log("Calling 'getAllRelations' Operation.");
		return searchOperations.getAllRelations(graphId, request);
	}

	@Override
	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		LOGGER.log("Calling 'getRelationProperty' Operation.");
		return searchOperations.getRelationProperty(graphId, startNodeId, relationType, endNodeId, key, request);
	}

	@Override
	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		LOGGER.log("Calling 'getRelation' Operation.");
		return searchOperations.getRelation(graphId, startNodeId, relationType, endNodeId, request);
	}

	@Override
	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		LOGGER.log("Calling 'checkCyclicLoop' Operation.");
		return searchOperations.checkCyclicLoop(graphId, startNodeId, relationType, endNodeId, request);
	}

	@Override
	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		LOGGER.log("Calling 'executeQuery' Operation.");
		return searchOperations.executeQuery(graphId, query, paramMap, request);
	}

	@Override
	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		LOGGER.log("Calling 'searchNodes' Operation.");
		return searchOperations.searchNodes(graphId, searchCriteria, getTags, request);
	}

	@Override
	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		LOGGER.log("Calling 'getNodesCount' Operation.");
		return searchOperations.getNodesCount(graphId, searchCriteria, request);
	}

	@Override
	public SubGraph traverse(String graphId, Traverser traverser, Request request) {
		LOGGER.log("Calling 'traverse' Operation.");
		return searchOperations.traverse(graphId, traverser, request);
	}

	@Override
	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		LOGGER.log("Calling 'traverseSubGraph' Operation.");
		return searchOperations.traverseSubGraph(graphId, traverser, request);
	}

	@Override
	public Graph getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request) {
		LOGGER.log("Calling 'getSubGraph' Operation.");
		return searchOperations.getSubGraph(graphId, startNodeId, relationType, depth, request);
	}

	@Override
	public Node upsertRootNode(String graphId, Request request) {
		LOGGER.log("Calling 'traverseSubGraph' Operation.");
		return nodeOperations.upsertRootNode(graphId, request);
	}
	
}
