package org.ekstep.graph.service;

import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.importer.ImportData;

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

	public void importGraph(String graphId, String taskId, ImportData input, Map<String, List<String>> messages, Request request);

	// Node Management APIs

	public void upsertNode(String graphId, Node node, Request request);

	public void addNode(String graphId, Node node, Request request);

	public void updateNode(String graphId, Node node, Request request);

	public void importNodes(String graphId, List<Node> nodes, Request request);

	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request);

	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request);

	public void removePropertyValue(String graphId, String nodeId, String key, Request request);

	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request);

	public void deleteNode(String graphId, String nodeId, Request request);

	// Search Management APIs

	public void getNodeById(String graphId, Long nodeId, Boolean getTags, Request request);

	public void getNodeByUniqueId(String graphId, Long nodeId, Boolean getTags, Request request);

	public void getNodesByProperty(String graphId, Property property, Boolean getTags, Request request);

	public void getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request);

	public void getNodeProperty(String graphId, String nodeId, String key, Request request);

	public void getAllNodes(String graphId, Request request);

	public void getAllRelations(String graphId, Request request);

	public void getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request);

	public void getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request);

	public void checkCyclicLoop(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request);

	public void executeQuery(String graphId, String query, Map<String, Object> paramMap, Request request);

	public void searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request);

	public void getNodesCount(String graphId, SearchCriteria searchCriteria, Request request);

	public void traverse(String graphId, Traverser traverser, Request request);

	public void traverseSubGraph(String graphId, Traverser traverser, Request request);

	public void getSubGraph(String graphId, String startNodeId, String relationType, int depth, Request request);

}
