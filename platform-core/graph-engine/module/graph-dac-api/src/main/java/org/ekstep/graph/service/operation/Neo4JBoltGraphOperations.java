package org.ekstep.graph.service.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.cache.mgr.impl.NodeCacheManager;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.common.Identifier;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.importer.ImportData;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.GraphOperation;
import org.ekstep.graph.service.util.DriverUtil;
import org.ekstep.graph.service.util.GraphQueryGenerationUtil;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ClientException;

public class Neo4JBoltGraphOperations {

	private final static String DEFAULT_CYPHER_NODE_OBJECT = "n";
	/**
	 * Creates the graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 */
	public static void createGraph(String graphId, Request request) {
		TelemetryManager.log("Operation Not Allowed in Bolt.");
	}

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
	public static void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		TelemetryManager.log("Graph Id: " + graphId);
		TelemetryManager.log("Index Properties List: " + indexProperties);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Create Graph Unique Contraint' Operation Failed.]");

		if (null == indexProperties || indexProperties.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
							+ " | ['Create Graph Unique Contraint' Operation Failed.]");
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			for (String indexProperty : indexProperties) {
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.indexProperty.name(), indexProperty);
				parameterMap.put(GraphDACParams.request.name(), request);

				try ( Transaction tx = session.beginTransaction() ){
					StatementResult result = tx.run(GraphQueryGenerationUtil.generateCreateUniqueConstraintCypherQuery(parameterMap));
					tx.success();
					for (Record record : result.list()) {
						TelemetryManager.log("'Create Unique' Constraint Operation Finished.", record.asMap());
					}
				}
			}
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

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
	public static void createIndex(String graphId, List<String> indexProperties, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Graph Index' Operation Failed.]");

		if (null == indexProperties || indexProperties.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
							+ " | [Create Graph Index Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {

			for (String indexProperty : indexProperties) {
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.indexProperty.name(), indexProperty);
				parameterMap.put(GraphDACParams.request.name(), request);

				try ( Transaction tx = session.beginTransaction() ) {
					StatementResult result = tx.run(GraphQueryGenerationUtil.generateCreateIndexCypherQuery(parameterMap));
					tx.success();
					for (Record record : result.list()) {
						TelemetryManager.log("'Create Index' Operation Finished.", record.asMap());
					}
				}
			}
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

	/**
	 * Delete graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 */
	public static void deleteGraph(String graphId, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Delete Graph' Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.request.name(), request);

			try ( Transaction tx = session.beginTransaction() ) {
				StatementResult result = tx.run(GraphQueryGenerationUtil.generateDeleteGraphCypherQuery(parameterMap));
				tx.success();
				for (Record record : result.list()) {
					TelemetryManager.log("'Delete Graph' Operation Finished.", record.asMap());
				}
			}
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

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
	@SuppressWarnings("unchecked")
	public static void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Relation' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Create Relation' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Create Relation' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Create Relation' Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.request.name(), request);

			GraphQueryGenerationUtil.generateCreateRelationCypherQuery(parameterMap);
			String query = (String) parameterMap.get(GraphDACParams.query.name());
			Map<String, Object> paramValuesMap = (Map<String, Object>) parameterMap
					.get(GraphDACParams.paramValueMap.name());

			if (StringUtils.isNotBlank(query)) {
				try ( Transaction tx = session.beginTransaction() ){
					//result = session.run(query, paramValuesMap);
					StatementResult result;
					if (null != paramValuesMap && !paramValuesMap.isEmpty())
						result = tx.run(query, paramValuesMap);
					else
						result = tx.run(query);
					tx.success();
					for (Record record : result.list())
						TelemetryManager.log("'Create Relation' Operation Finished.", record.asMap());
				}
				NodeCacheManager.deleteDataNode(graphId, startNodeId);
				NodeCacheManager.deleteDataNode(graphId, endNodeId);
			}
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

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
	@SuppressWarnings("unchecked")
	public static void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Update Relation' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Update Relation' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Update Relation' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Update Relation' Operation Failed.]");

		Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
		if (null != metadata && !metadata.isEmpty()) {
			Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
			TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.request.name(), request);

				GraphQueryGenerationUtil.generateUpdateRelationCypherQuery(parameterMap);
				String query = (String) parameterMap.get(GraphDACParams.query.name());
				Map<String, Object> paramValuesMap = (Map<String, Object>) parameterMap
						.get(GraphDACParams.paramValueMap.name());

				if (StringUtils.isNotBlank(query)) {
					try ( Transaction tx = session.beginTransaction() ){
						//result = session.run(query, paramValuesMap);
						StatementResult result;
						if (null != paramValuesMap && !paramValuesMap.isEmpty())
							result = tx.run(query, paramValuesMap);
						else
							result = tx.run(query);
						tx.success();
						for (Record record : result.list()) {
							TelemetryManager.log("'Update Relation' Operation Finished.", record.asMap());
						}
					}
					NodeCacheManager.deleteDataNode(graphId, startNodeId);
					NodeCacheManager.deleteDataNode(graphId, endNodeId);
				}
			} catch (Exception e) {
				throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
						DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
			}
		}
	}

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
	public static void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Delete Relation' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Delete Relation' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Delete Relation' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Delete Relation' Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.request.name(), request);

			try ( Transaction tx = session.beginTransaction() ) {
				StatementResult result = tx.run(GraphQueryGenerationUtil.generateDeleteRelationCypherQuery(parameterMap));
				tx.success();
				for (Record record : result.list()) {
					TelemetryManager.log("'Delete Relation' Operation Finished.", record.asMap());
				}
			}
			NodeCacheManager.deleteDataNode(graphId, startNodeId);
			NodeCacheManager.deleteDataNode(graphId, endNodeId);
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

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
	public static void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Incoming Relations' Operation Failed.]");

		if (null == startNodeIds || startNodeIds.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID_LIST
							+ " | ['Create Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID
							+ " | ['Create Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE
							+ " | ['Create Incoming Relations' Operation Failed.]");

		// CHECK - write driver and session created for this - START
		for (String startNodeId : startNodeIds)
			createRelation(graphId, startNodeId, endNodeId, relationType, request);
		// CHECK - write driver and session created for this - END
	}

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
	public static void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Outgoing Relations' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID
							+ " | ['Create Outgoing Relations' Operation Failed.]");

		if (null == endNodeIds || endNodeIds.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID_LIST
							+ " | ['Create Outgoing Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE
							+ " | ['Create Outgoing Relations' Operation Failed.]");

		// CHECK - write driver and session created for this - START
		TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
		for (String endNodeId : endNodeIds)
			createRelation(graphId, startNodeId, endNodeId, relationType, request);
		// CHECK - write driver and session created for this - END
	}

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
	public static void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Delete Incoming Relations' Operation Failed.]");

		if (null == startNodeIds || startNodeIds.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID_LIST
							+ " | ['Delete Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID
							+ " | ['Delete Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE
							+ " | ['Delete Incoming Relations' Operation Failed.]");

		// CHECK - write driver and session created for this - START
		TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
		for (String startNodeId : startNodeIds)
			deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
		// CHECK - write driver and session created for this - END
	}

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
	public static void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Delete Outgoing Relations' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID_LIST
							+ " | ['Delete Outgoing Relations' Operation Failed.]");

		if (null == endNodeIds || endNodeIds.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID
							+ " | ['Delete Outgoing Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE
							+ " | ['Delete Outgoing Relations' Operation Failed.]");

		TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
		for (String endNodeId : endNodeIds)
			deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
	}

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
	public static void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId,
			String relationType,
			String key, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Remove Relation Metadata' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID
							+ " | ['Remove Relation Metadata' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Remove Relation Metadata' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE
							+ " | ['Remove Relation Metadata' Operation Failed.]");

		if (StringUtils.isBlank(key))
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY_KEY
							+ " | ['Remove Relation Metadata' Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.key.name(), key);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(GraphQueryGenerationUtil.generateRemoveRelationMetadataCypherQuery(parameterMap));
			for (Record record : result.list()) {
				TelemetryManager.log("'Remove Relation Metadata' Operation Finished.", record.asMap());
			}
			NodeCacheManager.deleteDataNode(graphId, startNodeId);
			NodeCacheManager.deleteDataNode(graphId, endNodeId);
		}
	}

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
	public static void createCollection(String graphId, String collectionId, Node collection,
			String relationType, List<String> members, String indexProperty, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Collection' Operation Failed.]");

		// if (null == collection)
		// throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
		// DACErrorMessageConstants.INVALID_COLLECTION_NODE + " | ['Create
		// Collection' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Create Collection' Operation Failed.]");

		if (null != collection) {
			if (StringUtils.isBlank(collection.getIdentifier()))
				collection.setIdentifier(collectionId);
			Neo4JBoltNodeOperations.upsertNode(graphId, collection, request);
		}

		if (null != members && !members.isEmpty())
			createOutgoingRelations(graphId, collectionId, members, relationType, request);
	}

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
	public static void deleteCollection(String graphId, String collectionId, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Collection' Operation Failed.]");

		if (StringUtils.isBlank(collectionId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_COLLECTION_NODE_ID + " | ['Create Collection' Operation Failed.]");

		Neo4JBoltNodeOperations.deleteNode(graphId, collectionId, request);
	}

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
	public static Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input,
			Request request)
			throws Exception {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Import Graph' Operation Failed.]");
		if (null == input)
			throw new ClientException(DACErrorCodeConstants.INVALID_DATA.name(),
					DACErrorMessageConstants.INVALID_IMPORT_DATA + " | ['Import Graph' Operation Failed.]");
		Map<String, List<String>> messages = new HashMap<String, List<String>>();
		// CHECK - write driver, session and transaction created for this - START
		Map<String, org.ekstep.graph.dac.model.Node> existingNodes = new HashMap<String, org.ekstep.graph.dac.model.Node>();
		Map<String, Map<String, List<Relation>>> existingRelations = new HashMap<String, Map<String, List<Relation>>>();
		List<org.ekstep.graph.dac.model.Node> importedNodes = new ArrayList<org.ekstep.graph.dac.model.Node>(
				input.getDataNodes());
		int nodesCount = createNodes(graphId, request, existingNodes, existingRelations, importedNodes);
		int relationsCount = createRelations(graphId, request, existingRelations, existingNodes, importedNodes,
				messages);
		upsertRootNode(graphId, nodesCount, relationsCount, request);
		if (StringUtils.isNotBlank(taskId))
			updateTaskStatus(graphId, taskId, request);
		// CHECK - write driver and session created for this - END
		return messages;
	}

	private static void updateTaskStatus(String graphId, String taskId, Request request) throws Exception {
		Node taskNode = new Node();
		taskNode.setGraphId(graphId);
		taskNode.setIdentifier(taskId);
		taskNode.setMetadata(new HashMap<String, Object>());
		taskNode.getMetadata().put(GraphEngineParams.status.name(), GraphEngineParams.Completed.name());
		Neo4JBoltNodeOperations.upsertNode(graphId, taskNode, request);
	}

	private static int createNodes(String graphId, Request request, Map<String, Node> existingNodes,
			Map<String, Map<String, List<Relation>>> existingRelations, List<Node> nodes) {
		int nodesCount = 0;
		for (Node node : nodes) {
			if (null == node || StringUtils.isBlank(node.getIdentifier()) || StringUtils.isBlank(node.getNodeType())) {
				// ERROR(GraphDACErrorCodes.ERR_CREATE_NODE_MISSING_REQ_PARAMS.name(),
				// "Invalid input node", request, getSender());
			} else {
				Node neo4jNode = null;
				if (existingNodes.containsKey(node.getIdentifier())) {
					neo4jNode = existingNodes.get(node.getIdentifier());
				} else {
					neo4jNode = Neo4JBoltNodeOperations.upsertNode(graphId, node, request);
					nodesCount++;
				}
				neo4jNode = Neo4JBoltSearchOperations.getNodeByUniqueId(graphId, node.getIdentifier(), true, request);
				existingNodes.put(node.getIdentifier(), neo4jNode);
				List<Relation> relations = new ArrayList<Relation>();
				if (null != neo4jNode.getOutRelations())
					relations.addAll(neo4jNode.getOutRelations());
				if (null != neo4jNode.getInRelations())
					relations.addAll(neo4jNode.getInRelations());
				getExistingRelations(relations, existingRelations);
			}
		}
		return nodesCount;
	}

	private static Map<String, Map<String, List<Relation>>> getExistingRelations(List<Relation> dbRelations,
			Map<String, Map<String, List<Relation>>> existingRelations) {
		if (null != dbRelations && null != dbRelations.iterator()) {
			for (Relation relationship : dbRelations) {
				String startNodeId = relationship.getStartNodeId();
				String relationType = relationship.getRelationType();
				if (existingRelations.containsKey(startNodeId)) {
					Map<String, List<Relation>> relationMap = existingRelations.get(startNodeId);
					if (relationMap.containsKey(relationType)) {
						List<Relation> relationList = relationMap.get(relationType);
						relationList.add(relationship);
					} else {
						List<Relation> relationList = new ArrayList<Relation>();
						relationList.add(relationship);
						relationMap.put(relationType, relationList);
					}
				} else {
					Map<String, List<Relation>> relationMap = new HashMap<String, List<Relation>>();
					List<Relation> relationList = new ArrayList<Relation>();
					relationList.add(relationship);
					relationMap.put(relationType, relationList);
					existingRelations.put(startNodeId, relationMap);
				}
			}
		}
		return existingRelations;
	}

	private static int createRelations(String graphId, Request request,
			Map<String, Map<String, List<Relation>>> existingRelations,
			Map<String, Node> existingNodes, List<Node> nodes,
			Map<String, List<String>> messages) {
		int relationsCount = 0;
		for (Node node : nodes) {
			List<Relation> nodeRelations = node.getOutRelations();
			if (nodeRelations != null) {
				Map<String, List<String>> nodeRelMap = new HashMap<String, List<String>>();
				Map<String, Map<String, Relation>> nodeRelation = new HashMap<String, Map<String, Relation>>();
				for (Relation rel : nodeRelations) {
					String relType = rel.getRelationType();
					Map<String, Relation> relMap = nodeRelation.get(relType);
					if (null == relMap) {
						relMap = new HashMap<String, Relation>();
						nodeRelation.put(relType, relMap);
					}
					if (nodeRelMap.containsKey(relType)) {
						List<String> endNodeIds = nodeRelMap.get(relType);
						if (endNodeIds == null) {
							endNodeIds = new ArrayList<String>();
							nodeRelMap.put(relType, endNodeIds);
						}
						if (StringUtils.isNotBlank(rel.getEndNodeId())) {
							endNodeIds.add(rel.getEndNodeId().trim());
							relMap.put(rel.getEndNodeId().trim(), rel);
						}
					} else {
						List<String> endNodeIds = new ArrayList<String>();
						if (StringUtils.isNotBlank(rel.getEndNodeId())) {
							endNodeIds.add(rel.getEndNodeId().trim());
							relMap.put(rel.getEndNodeId().trim(), rel);
						}
						nodeRelMap.put(relType, endNodeIds);
					}
				}
				String uniqueId = node.getIdentifier();
				Node neo4jNode = existingNodes.get(uniqueId);
				if (existingRelations.containsKey(uniqueId)) {
					Map<String, List<Relation>> relationMap = existingRelations.get(uniqueId);
					for (String relType : relationMap.keySet()) {
						if (nodeRelMap.containsKey(relType)) {
							List<String> relEndNodeIds = nodeRelMap.get(relType);
							Map<String, Relation> relMap = nodeRelation.get(relType);
							for (Relation rel : relationMap.get(relType)) {
								String endNodeId = rel.getEndNodeId();
								if (relEndNodeIds.contains(endNodeId)) {
									relEndNodeIds.remove(endNodeId);
									Relation relation = relMap.get(endNodeId);
									request.put(GraphDACParams.metadata.name(), relation.getMetadata());
									updateRelation(graphId, rel.getStartNodeId(), rel.getEndNodeId(),
											rel.getRelationType(), request);
								} else {
									deleteRelation(graphId, rel.getStartNodeId(), rel.getEndNodeId(),
											rel.getRelationType(), request);
									relationsCount--;
								}
							}
							for (String endNodeId : relEndNodeIds) {
								Node otherNode = existingNodes.get(endNodeId);
								if (otherNode != null) {
									Relation relation = relMap.get(endNodeId);
									request.put(GraphDACParams.metadata.name(), relation.getMetadata());
									createRelation(graphId, neo4jNode.getIdentifier(), otherNode.getIdentifier(),
											relType, request);
									relationsCount++;
								} else {
									otherNode = Neo4JBoltSearchOperations.getNodeByUniqueId(graphId, endNodeId, true,
											request);
									if (null == otherNode) {
										List<String> rowMsgs = messages.get(uniqueId);
										if (rowMsgs == null) {
											rowMsgs = new ArrayList<String>();
											messages.put(uniqueId, rowMsgs);
										}
										rowMsgs.add("Node with id: " + endNodeId + " not found to create relation:"
												+ relType);
									} else {
										existingNodes.put(endNodeId, otherNode);
										Relation relation = relMap.get(endNodeId);
										request.put(GraphDACParams.metadata.name(), relation.getMetadata());
										createRelation(graphId, neo4jNode.getIdentifier(), otherNode.getIdentifier(),
												relType, request);
										relationsCount++;
									}
								}

							}
						} else {
							for (Relation rel : relationMap.get(relType)) {
								deleteRelation(graphId, rel.getStartNodeId(), rel.getEndNodeId(), rel.getRelationType(),
										request);
								relationsCount--;
							}
						}
					}
					for (String relType : nodeRelMap.keySet()) {
						if (!relationMap.containsKey(relType)) {
							relationsCount += createNewRelations(neo4jNode, nodeRelMap, relType, nodeRelation, uniqueId,
									existingNodes, messages, graphId, request);
						}
					}
				} else {
					for (String relType : nodeRelMap.keySet()) {
						relationsCount += createNewRelations(neo4jNode, nodeRelMap, relType, nodeRelation, uniqueId,
								existingNodes, messages, graphId, request);
					}
				}
			}
		}
		return relationsCount;
	}

	private static int createNewRelations(Node neo4jNode, Map<String, List<String>> nodeRelMap,
			String relType, Map<String, Map<String, Relation>> nodeRelation, String uniqueId,
			Map<String, Node> existingNodes, Map<String, List<String>> messages,
			String graphId, Request request) {
		int relationsCount = 0;
		List<String> relEndNodeIds = nodeRelMap.get(relType);
		Map<String, Relation> relMap = nodeRelation.get(relType);
		for (String endNodeId : relEndNodeIds) {
			Node otherNode = existingNodes.get(endNodeId);
			if (null == otherNode) {
				otherNode = Neo4JBoltSearchOperations.getNodeByUniqueId(graphId, endNodeId, true, request);
				if (null == otherNode) {
					List<String> rowMsgs = messages.get(uniqueId);
					if (rowMsgs == null) {
						rowMsgs = new ArrayList<String>();
						messages.put(uniqueId, rowMsgs);
					}
					rowMsgs.add("Node with id: " + endNodeId + " not found to create relation:" + relType);
				} else {
					existingNodes.put(endNodeId, otherNode);
					Relation relation = relMap.get(endNodeId);
					request.put(GraphDACParams.metadata.name(), relation.getMetadata());
					createRelation(graphId, neo4jNode.getIdentifier(), otherNode.getIdentifier(), relType, request);
					relationsCount++;
				}
			} else {
				Relation relation = relMap.get(endNodeId);
				request.put(GraphDACParams.metadata.name(), relation.getMetadata());
				createRelation(graphId, neo4jNode.getIdentifier(), otherNode.getIdentifier(), relType, request);
				relationsCount++;
			}
		}
		return relationsCount;
	}

	private static void upsertRootNode(String graphId, Integer nodesCount, Integer relationsCount, Request request) {
		String rootNodeUniqueId = Identifier.getIdentifier(graphId, SystemNodeTypes.ROOT_NODE.name());
		Node node = null;
		try {
			node = Neo4JBoltSearchOperations.getNodeByUniqueId(graphId, rootNodeUniqueId, true, request);
		} catch(ResourceNotFoundException e) {
			if (null == node) {
				node = new Node();
				node.setGraphId(graphId);
				node.setIdentifier(rootNodeUniqueId);
				node.setMetadata(new HashMap<String, Object>());
			}
		}
		node.getMetadata().put(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.ROOT_NODE.name());

		Long dbNodesCount = (Long) node.getMetadata().get("nodesCount");
		if (null == dbNodesCount)
			dbNodesCount = 0l;
		Long dbRelationsCount = (Long) node.getMetadata().get("relationsCount");
		if (null == dbRelationsCount)
			dbRelationsCount = 0l;
		node.getMetadata().put("nodesCount", dbNodesCount + nodesCount);
		node.getMetadata().put("relationsCount", dbRelationsCount + relationsCount);
		Neo4JBoltNodeOperations.upsertNode(graphId, node, request);
	}

	public static void bulkUpdateNodes(String graphId, List<Map<String, Object>> newNodes,
			List<Map<String, Object>> modifiedNodes,
			List<Map<String, Object>> addOutRelations, List<Map<String, Object>> removeOutRelations,
			List<Map<String, Object>> addInRelations, List<Map<String, Object>> removeInRelations) {
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		Transaction tr = null;
		List<Record> listNode = new ArrayList<>();
		try (Session session = driver.session()) {
			tr = session.beginTransaction();
			createNodes(tr, graphId, newNodes, listNode);
			updateNodes(tr, graphId, modifiedNodes, listNode);
			removeOutRelations(tr, graphId, removeOutRelations);
			removeInRelations(tr, graphId, removeInRelations);
			addOutRelations(tr, graphId, addOutRelations);
			addInRelations(tr, graphId, addInRelations);
			tr.success();
			updateRedisCache(graphId, listNode);
		} catch (Exception e) {
			if (null != tr)
				tr.failure();
			throw new ServerException("ERR_BULK_UPDATE_OPERATION", "Bulk update operation failed: " + e.getMessage());
		} finally {
			if (null != tr)
				tr.close();
		}
	}
	
	
	
	private static void createNodes(Transaction tr, String graphId, List<Map<String, Object>> nodes, List<Record> listNode) {
		if (null != nodes && !nodes.isEmpty()) {
			TelemetryManager.info("Bulk update | Creating nodes : " + nodes.size());
			String query = "UNWIND {batch} as row CREATE (n:" + graphId + ") SET n += row RETURN n";
			Map<String, Object> params = new HashMap<String, Object>(){{put("batch", nodes);}};
			StatementResult result = tr.run(query, params);
			if(null!=result && result.hasNext())
				listNode.addAll(result.list());
		}
	}

	private static void updateNodes(Transaction tr, String graphId, List<Map<String, Object>> nodes, List<Record> listNode) {
		if (null != nodes && !nodes.isEmpty()) {
			TelemetryManager.info("Bulk update | Updating nodes : " + nodes.size());
			String query = "UNWIND {batch} as row MATCH (n:" + graphId
					+ "{IL_UNIQUE_ID: row.IL_UNIQUE_ID}) SET n += row.metadata RETURN n";
			Map<String, Object> params = new HashMap<String, Object>(){{put("batch", nodes);}};
			StatementResult result = tr.run(query, params);
			if(null!=result && result.hasNext())
				listNode.addAll(result.list());
		}
	}

	private static void addOutRelations(Transaction tr, String graphId, List<Map<String, Object>> relations) {
		if (null != relations && !relations.isEmpty()) {
			TelemetryManager.info("Bulk update | Adding out relations : " + relations.size());
			Map<String, List<Map<String, Object>>> relationTypeMap = getRelationMap(relations);
			for (Entry<String, List<Map<String, Object>>> entry : relationTypeMap.entrySet()) {
				String query = "UNWIND {batch} as row MATCH (from:" + graphId + "{IL_UNIQUE_ID: row.from}) MATCH (to:"
						+ graphId + "{IL_UNIQUE_ID: row.to}) CREATE (from)-[rel:" + entry.getKey()
						+ "]->(to) SET rel += row.metadata";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("batch", entry.getValue());
				tr.run(query, params);
			}
		}
	}
	
	private static void addInRelations(Transaction tr, String graphId, List<Map<String, Object>> relations) {
		if (null != relations && !relations.isEmpty()) {
			TelemetryManager.info("Bulk update | Adding in relations : " + relations.size());
			Map<String, List<Map<String, Object>>> relationTypeMap = getRelationMap(relations);
			for (Entry<String, List<Map<String, Object>>> entry : relationTypeMap.entrySet()) {
				String query = "UNWIND {batch} as row MATCH (from:" + graphId + "{IL_UNIQUE_ID: row.from}) MATCH (to:"
						+ graphId + "{IL_UNIQUE_ID: row.to}) CREATE (from)<-[rel:" + entry.getKey()
						+ "]-(to) SET rel += row.metadata";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("batch", entry.getValue());
				tr.run(query, params);
			}
		}
	}

	private static void removeOutRelations(Transaction tr, String graphId, List<Map<String, Object>> relations) {
		if (null != relations && !relations.isEmpty()) {
			TelemetryManager.info("Bulk update | Removing out relations : " + relations.size());
			Map<String, List<Map<String, Object>>> relationTypeMap = getRelationMap(relations);
			for (Entry<String, List<Map<String, Object>>> entry : relationTypeMap.entrySet()) {
				String query = "UNWIND {batch} as row MATCH (from:" + graphId + "{IL_UNIQUE_ID: row.IL_UNIQUE_ID})-[r:"
						+ entry.getKey() + "]->(to:" + graphId + " {IL_FUNC_OBJECT_TYPE: row.objectType}) DELETE r";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("batch", entry.getValue());
				tr.run(query, params);
			}
		}
	}

	private static void removeInRelations(Transaction tr, String graphId, List<Map<String, Object>> relations) {
		if (null != relations && !relations.isEmpty()) {
			TelemetryManager.info("Bulk update | Removing in relations : " + relations.size());
			Map<String, List<Map<String, Object>>> relationTypeMap = getRelationMap(relations);
			for (Entry<String, List<Map<String, Object>>> entry : relationTypeMap.entrySet()) {
				String query = "UNWIND {batch} as row MATCH (from:" + graphId
						+ "{IL_FUNC_OBJECT_TYPE: row.objectType})<-[r:" + entry.getKey() + "]-(to:" + graphId
						+ " {IL_UNIQUE_ID: row.IL_UNIQUE_ID}) DELETE r";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("batch", entry.getValue());
				tr.run(query, params);
			}
		}
	}

	private static Map<String, List<Map<String, Object>>> getRelationMap(List<Map<String, Object>> relations) {
		Map<String, List<Map<String, Object>>> relationTypeMap = null;
		if (null != relations) {
			relationTypeMap = new HashMap<String, List<Map<String, Object>>>();
			for (Map<String, Object> relation : relations) {
				String type = (String) relation.get("type");
				List<Map<String, Object>> list = relationTypeMap.get(type);
				if (null == list)
					list = new ArrayList<Map<String, Object>>();
				list.add(relation);
				relationTypeMap.put(type, list);
			}
		}
		return relationTypeMap;
	}
	private static void updateRedisCache(String graphId, List<Record> listNode) {
		if (!graphId.equalsIgnoreCase("domain"))
			return;
		if(!listNode.isEmpty())
			for (Record record : listNode) {
				try {
					org.neo4j.driver.v1.types.Node neo4JNode = record.get(DEFAULT_CYPHER_NODE_OBJECT).asNode();
					updateRedisCache(graphId, neo4JNode);
				} catch (Exception e) {
					throw new ServerException(DACErrorCodeConstants.CACHE_ERROR.name(),
							DACErrorMessageConstants.CACHE_ERROR + " | " + e.getMessage());
				}
			}
	}
	
	private static void updateRedisCache(String graphId, org.neo4j.driver.v1.types.Node neo4JNode){
		String nodeId = (String) neo4JNode.get(SystemProperties.IL_UNIQUE_ID.name()).asString();
		String nodeType = (String) neo4JNode.get(SystemProperties.IL_SYS_NODE_TYPE.name()).asString();
		
		if (!nodeType.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name()))
			return;
		
		Map<String, Object> cacheMap = new HashMap<>();
		if (StringUtils.isNotBlank(neo4JNode.get(GraphDACParams.versionKey.name()).asString()))
			cacheMap.put(GraphDACParams.versionKey.name(), neo4JNode.get(GraphDACParams.versionKey.name()).asString());
		if (StringUtils.isNotBlank(neo4JNode.get(GraphDACParams.consumerId.name()).asString()))
			cacheMap.put(GraphDACParams.consumerId.name(), neo4JNode.get(GraphDACParams.consumerId.name()).asString());
		if (StringUtils.isNotBlank(neo4JNode.get(GraphDACParams.lastUpdatedOn.name()).asString()))
			cacheMap.put(GraphDACParams.lastUpdatedOn.name(),
					neo4JNode.get(GraphDACParams.lastUpdatedOn.name()).asString());
		if (StringUtils.isNotBlank(neo4JNode.get(GraphDACParams.createdBy.name()).asString()))
			cacheMap.put(GraphDACParams.createdBy.name(), neo4JNode.get(GraphDACParams.createdBy.name()).asString());
		if (StringUtils.isNotBlank(neo4JNode.get(GraphDACParams.status.name()).asString()))
			cacheMap.put(GraphDACParams.status.name(), neo4JNode.get(GraphDACParams.status.name()).asString());

		if (cacheMap.size() > 0)
			RedisStoreUtil.saveNodeProperties(graphId, nodeId, cacheMap);
		NodeCacheManager.deleteDataNode(graphId, nodeId);
		
		
	}

}
