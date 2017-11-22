/**
 * 
 */
package org.ekstep.graph.service;

import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.graph.importer.ImportData;

/**
 * @author pradyumna
 *
 */
public interface INeo4JBoltGraphOperations {

	/**
	 * Creates the graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 */
	void createGraph(String graphId, Request request);

	/**
	 * Creates the graph unique contraint.
	 *
	 * @param graphId
	 *            the graph id
	 * @param indexProperties
	 *            the index properties
	 * @param request
	 *            the request
	 */
	void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request);

	/**
	 * Creates the index.
	 *
	 * @param graphId
	 *            the graph id
	 * @param indexProperties
	 *            the index properties
	 * @param request
	 *            the request
	 */
	void createIndex(String graphId, List<String> indexProperties, Request request);

	/**
	 * Delete graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 */
	void deleteGraph(String graphId, Request request);

	/**
	 * Creates the relation.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 * @param request
	 *            the request
	 */
	void createRelation(String graphId, String startNodeId, String endNodeId, String relationType, Request request);

	/**
	 * Update relation.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 * @param request
	 *            the request
	 */
	void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType, Request request);

	/**
	 * Delete relation.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 * @param request
	 *            the request
	 */
	void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType, Request request);

	/**
	 * Creates the incoming relations.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeIds
	 *            the start node ids
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 * @param request
	 *            the request
	 */
	void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId, String relationType,
			Request request);

	/**
	 * Creates the outgoing relations.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param endNodeIds
	 *            the end node ids
	 * @param relationType
	 *            the relation type
	 * @param request
	 *            the request
	 */
	void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds, String relationType,
			Request request);

	/**
	 * Delete incoming relations.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeIds
	 *            the start node ids
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 * @param request
	 *            the request
	 */
	void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId, String relationType,
			Request request);

	/**
	 * Delete outgoing relations.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param endNodeIds
	 *            the end node ids
	 * @param relationType
	 *            the relation type
	 * @param request
	 *            the request
	 */
	void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds, String relationType,
			Request request);

	/**
	 * Removes the relation metadata by key.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 * @param key
	 *            the key
	 * @param request
	 *            the request
	 */
	void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request);

	/**
	 * Creates the collection.
	 *
	 * @param graphId
	 *            the graph id
	 * @param collectionId
	 *            the collection id
	 * @param collection
	 *            the collection
	 * @param relationType
	 *            the relation type
	 * @param members
	 *            the members
	 * @param indexProperty
	 *            the index property
	 * @param request
	 *            the request
	 */
	void createCollection(String graphId, String collectionId, com.ilimi.graph.dac.model.Node collection,
			String relationType, List<String> members, String indexProperty, Request request);

	/**
	 * Delete collection.
	 *
	 * @param graphId
	 *            the graph id
	 * @param collectionId
	 *            the collection id
	 * @param request
	 *            the request
	 */
	void deleteCollection(String graphId, String collectionId, Request request);

	/**
	 * Import graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param taskId
	 *            the task id
	 * @param input
	 *            the input
	 * @param request
	 *            the request
	 * @return the map
	 * @throws Exception
	 *             the exception
	 */
	Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request)
			throws Exception;

	void bulkUpdateNodes(String graphId, List<Map<String, Object>> newNodes, List<Map<String, Object>> modifiedNodes,
			List<Map<String, Object>> addOutRelations, List<Map<String, Object>> removeOutRelations,
			List<Map<String, Object>> addInRelations, List<Map<String, Object>> removeInRelations);

}