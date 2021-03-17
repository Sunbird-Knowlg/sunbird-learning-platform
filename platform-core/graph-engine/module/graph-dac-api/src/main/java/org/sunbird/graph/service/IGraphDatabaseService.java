package org.sunbird.graph.service;

import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.graph.dac.model.Graph;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.dac.model.SubGraph;
import org.sunbird.graph.dac.model.Traverser;
import org.sunbird.graph.importer.ImportData;

/**
 * IGraphService is Contract between the Platform and Neo4J System
 *
 * @author Mohammad Azharuddin
 * @version 1.0
 * @since 2016-08-17
 */
public interface IGraphDatabaseService {

	// Graph Management APIs

	public void createGraph(String graphId, Request request);

	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request);

	public void createIndex(String graphId, List<String> indexProperties, Request request);

	public void deleteGraph(String graphId, Request request);

	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request);

	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request);

	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request);

	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request);

	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request);

	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request);

	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request);

	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request);

	public void createCollection(String graphId, String collectionId, Node collection, String relationType,
			List<String> members, String indexProperty, Request request);

	public void deleteCollection(String graphId, String collectionId, Request request);

	public Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request);

	// Node Management APIs

	public Node upsertNode(String graphId, Node node, Request request);

	public Node addNode(String graphId, Node node, Request request);

	public Node updateNode(String graphId, Node node, Request request);

	public void importNodes(String graphId, List<Node> nodes, Request request);

	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request);

	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request);

	public void removePropertyValue(String graphId, String nodeId, String key, Request request);

	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request);

	public void deleteNode(String graphId, String nodeId, Request request);
	
	public Node upsertRootNode(String graphId, Request request);

	// Search Management APIs

	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request);

	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request);

	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request);

	public List<Node> getNodesByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request);

	public Property getNodeProperty(String graphId, String nodeId, String key, Request request);

	public List<Node> getAllNodes(String graphId, Request request);

	public List<Relation> getAllRelations(String graphId, Request request);

	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request);

	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request);

	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request);

	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap, Request request);

	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request);

	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request);

	public SubGraph traverse(String graphId, Traverser traverser, Request request);

	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request);

	public Graph getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request);
	
	public void bulkUpdateNodes(String graphId, List<Map<String, Object>> newNodes, List<Map<String, Object>> modifiedNodes,
			List<Map<String, Object>> addOutRelations, List<Map<String, Object>> removeOutRelations,
			List<Map<String, Object>> addInRelations, List<Map<String, Object>> removeInRelations);

	public List<Map<String, Object>> executeQueryForProps(String graphId, String query, List<String> propKeys);

}
