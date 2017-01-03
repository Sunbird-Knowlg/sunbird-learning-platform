package org.ekstep.graph.service.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.Neo4JOperation;
import org.ekstep.graph.service.util.DriverUtil;
import org.ekstep.graph.service.util.QueryUtil;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.common.dto.Request;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.importer.ImportData;

public class Neo4JBoltGraphOperations {

	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltGraphOperations.class.getName());

	/**
	 * Creates the graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 */
	public void createGraph(String graphId, Request request) {
		LOGGER.info("Operation Not Allowed in Bolt.");
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
	public void createGraphUniqueContraint(String graphId, List<String> indexProperties, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Index Properties List: ", indexProperties);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Create Graph Unique Contraint' Operation Failed.]");

		if (null == indexProperties || indexProperties.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
							+ " | ['Create Graph Unique Contraint' Operation Failed.]");
		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");
			for (String indexProperty : indexProperties) {
				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.indexProperty.name(), indexProperty);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.CREATE_UNIQUE_CONSTRAINT, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Create Unique' Constraint Operation Finished.", record);
				}
			}
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
	public void createIndex(String graphId, List<String> indexProperties, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Index Properties List: ", indexProperties);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Graph Index' Operation Failed.]");

		if (null == indexProperties || indexProperties.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
							+ " | [Create Graph Index Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");
			
			for (String indexProperty : indexProperties) {
				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.indexProperty.name(), indexProperty);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.CREATE_INDEX, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Create Index' Operation Finished.", record);
				}
			}
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
	public void deleteGraph(String graphId, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Delete Graph' Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

			LOGGER.info("Populating Parameter Map.");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.DELETE_GRAPH, parameterMap));
			for (Record record : result.list()) {
				LOGGER.debug("'Delete Graph' Operation Finished.", record);
			}
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
	public void createRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Request: ", request);

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

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

			LOGGER.info("Populating Parameter Map.");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.CREATE_RELATION, parameterMap));
			for (Record record : result.list()) {
				LOGGER.debug("'Create Relation' Operation Finished.", record);
			}
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
	public void updateRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Request: ", request);

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
			Driver driver = DriverUtil.getDriver(graphId);
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.request.name(), request);

				String query = QueryUtil.getQuery(Neo4JOperation.UPDATE_RELATION, parameterMap);
				if (StringUtils.isNotBlank(query)) {
					StatementResult result = session.run(query);
					for (Record record : result.list()) {
						LOGGER.debug("'Update Relation' Operation Finished.", record);
					}
				}
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
	public void deleteRelation(String graphId, String startNodeId, String endNodeId, String relationType,
			Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Request: ", request);

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

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

			LOGGER.info("Populating Parameter Map.");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.DELETE_RELATION, parameterMap));
			for (Record record : result.list()) {
				LOGGER.debug("'Delete Relation' Operation Finished.", record);
			}
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
	public void createIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Ids: ", startNodeIds);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Request: ", request);

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

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");
			for (String startNodeId : startNodeIds)
				createRelation(graphId, startNodeId, endNodeId, relationType, request);
		}
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
	public void createOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("End Node Ids: ", endNodeIds);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Request: ", request);

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

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");
			for (String endNodeId : endNodeIds)
				createRelation(graphId, startNodeId, endNodeId, relationType, request);
		}
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
	public void deleteIncomingRelations(String graphId, List<String> startNodeIds, String endNodeId,
			String relationType, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Ids: ", startNodeIds);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Request: ", request);

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

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");
			for (String startNodeId : startNodeIds)
				deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
		}
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
	public void deleteOutgoingRelations(String graphId, String startNodeId, List<String> endNodeIds,
			String relationType, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("End Node Ids: ", endNodeIds);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Request: ", request);

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

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");
			for (String endNodeId : endNodeIds)
				deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
		}
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
	public void removeRelationMetadataByKey(String graphId, String startNodeId, String endNodeId, String relationType,
			String key, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Metadata Key: ", key);
		LOGGER.debug("Request: ", request);

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

		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

			LOGGER.info("Populating Parameter Map.");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.key.name(), key);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(QueryUtil.getQuery(Neo4JOperation.REMOVE_RELATION_METADATA, parameterMap));
			for (Record record : result.list()) {
				LOGGER.debug("'Remove Relation Metadata' Operation Finished.", record);
			}
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
	public void createCollection(String graphId, String collectionId, com.ilimi.graph.dac.model.Node collection,
			String relationType, List<String> members, String indexProperty, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Collection Node Id: ", collectionId);
		LOGGER.debug("Collection Node: ", collection);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Members: ", members);
		LOGGER.debug("Index Property: ", indexProperty);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Collection' Operation Failed.]");

		if (null == collection)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_COLLECTION_NODE + " | ['Create Collection' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Create Collection' Operation Failed.]");

		Neo4JBoltNodeOperations nodeOperations = new Neo4JBoltNodeOperations();
		if (StringUtils.isBlank(collection.getIdentifier()))
			collection.setIdentifier(collectionId);
		nodeOperations.upsertNode(graphId, collection, request);
		if (null != members && !members.isEmpty())
			createOutgoingRelations(graphId, collection.getIdentifier(), members, relationType, request);
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
	public void deleteCollection(String graphId, String collectionId, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Collection Node Id: ", collectionId);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Collection' Operation Failed.]");

		if (StringUtils.isBlank(collectionId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_COLLECTION_NODE_ID + " | ['Create Collection' Operation Failed.]");
		
		Neo4JBoltNodeOperations nodeOperations = new Neo4JBoltNodeOperations();
		nodeOperations.deleteNode(graphId, collectionId, request);
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
	public Map<String, List<String>> importGraph(String graphId, String taskId, ImportData input, Request request)
			throws Exception {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Task Id: ", taskId);
		LOGGER.debug("Import Data: ", input);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Import Graph' Operation Failed.]");
		if (null == input)
			throw new ClientException(DACErrorCodeConstants.INVALID_DATA.name(),
					DACErrorMessageConstants.INVALID_IMPORT_DATA + " | ['Import Graph' Operation Failed.]");
		Map<String, List<String>> messages = new HashMap<String, List<String>>();
		Driver driver = DriverUtil.getDriver(graphId);
		try (Session session = driver.session()) {
			try (org.neo4j.driver.v1.Transaction tx = session.beginTransaction()) {
				Map<String, com.ilimi.graph.dac.model.Node> existingNodes = new HashMap<String, com.ilimi.graph.dac.model.Node>();
				Map<String, Map<String, List<Relation>>> existingRelations = new HashMap<String, Map<String, List<Relation>>>();
				List<com.ilimi.graph.dac.model.Node> importedNodes = new ArrayList<com.ilimi.graph.dac.model.Node>(
						input.getDataNodes());
				int nodesCount = createNodes(graphId, request, existingNodes, existingRelations, importedNodes);
				int relationsCount = createRelations(graphId, request, existingRelations, existingNodes, importedNodes,
						messages);
				upsertRootNode(graphId, nodesCount, relationsCount, request);
				if (StringUtils.isNotBlank(taskId))
					updateTaskStatus(graphId, taskId, request);
				tx.success();
			}
		}
		return messages;
	}
	
	private void updateTaskStatus(String graphId, String taskId, Request request) throws Exception {
		Neo4JBoltNodeOperations nodeOps = new Neo4JBoltNodeOperations();
		com.ilimi.graph.dac.model.Node taskNode = new com.ilimi.graph.dac.model.Node();
		taskNode.setGraphId(graphId);
		taskNode.setIdentifier(taskId);
		taskNode.setMetadata(new HashMap<String, Object>());
		taskNode.getMetadata().put(GraphEngineParams.status.name(), GraphEngineParams.Completed.name());
		nodeOps.upsertNode(graphId, taskNode, request);
	}
	
	private int createNodes(String graphId, Request request, Map<String, com.ilimi.graph.dac.model.Node> existingNodes, 
			Map<String, Map<String, List<Relation>>> existingRelations,
			List<com.ilimi.graph.dac.model.Node> nodes) {
		Neo4JBoltSearchOperations searchOps = new Neo4JBoltSearchOperations();
		Neo4JBoltNodeOperations nodeOps = new Neo4JBoltNodeOperations();
		int nodesCount = 0;
		for (com.ilimi.graph.dac.model.Node node : nodes) {
			if (null == node || StringUtils.isBlank(node.getIdentifier()) || StringUtils.isBlank(node.getNodeType())) {
				// ERROR(GraphDACErrorCodes.ERR_CREATE_NODE_MISSING_REQ_PARAMS.name(),
				// "Invalid input node", request, getSender());
			} else {
				com.ilimi.graph.dac.model.Node neo4jNode = null;
				if (existingNodes.containsKey(node.getIdentifier())) {
					neo4jNode = existingNodes.get(node.getIdentifier());
				} else {
					neo4jNode = nodeOps.upsertNode(graphId, node, request);
					nodesCount++;
				}
				neo4jNode = searchOps.getNodeByUniqueId(graphId, node.getIdentifier(), true, request);
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
	
	private Map<String, Map<String, List<Relation>>> getExistingRelations(List<Relation> dbRelations,
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
	
	private int createRelations(String graphId, Request request, 
			Map<String, Map<String, List<Relation>>> existingRelations, Map<String, com.ilimi.graph.dac.model.Node> existingNodes,
			List<com.ilimi.graph.dac.model.Node> nodes, Map<String, List<String>> messages) {
		int relationsCount = 0;
		Neo4JBoltSearchOperations searchOps = new Neo4JBoltSearchOperations();
		for (com.ilimi.graph.dac.model.Node node : nodes) {
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
				// System.out.println("nodeRelMap:"+nodeRelMap);
				String uniqueId = node.getIdentifier();
				com.ilimi.graph.dac.model.Node neo4jNode = existingNodes.get(uniqueId);
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
									updateRelation(graphId, rel.getStartNodeId(), rel.getEndNodeId(), rel.getRelationType(), request);
								} else {
									deleteRelation(graphId, rel.getStartNodeId(), rel.getEndNodeId(), rel.getRelationType(), request);
									relationsCount--;
								}
							}
							for (String endNodeId : relEndNodeIds) {
								com.ilimi.graph.dac.model.Node otherNode = existingNodes.get(endNodeId);
								if (otherNode != null) {
									Relation relation = relMap.get(endNodeId);
									request.put(GraphDACParams.metadata.name(), relation.getMetadata());
									createRelation(graphId, neo4jNode.getIdentifier(), otherNode.getIdentifier(), relType, request);
									relationsCount++;
								} else {
									otherNode = searchOps.getNodeByUniqueId(graphId, endNodeId, true, request);
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
										createRelation(graphId, neo4jNode.getIdentifier(), otherNode.getIdentifier(), relType, request);
										relationsCount++;
									}
								}

							}
						} else {
							for (Relation rel : relationMap.get(relType)) {
								deleteRelation(graphId, rel.getStartNodeId(), rel.getEndNodeId(), rel.getRelationType(), request);
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

	private int createNewRelations(com.ilimi.graph.dac.model.Node neo4jNode, Map<String, List<String>> nodeRelMap, String relType,
			Map<String, Map<String, Relation>> nodeRelation, String uniqueId, Map<String, com.ilimi.graph.dac.model.Node> existingNodes,
			Map<String, List<String>> messages, String graphId, Request request) {
		int relationsCount = 0;
		Neo4JBoltSearchOperations searchOps = new Neo4JBoltSearchOperations();
		List<String> relEndNodeIds = nodeRelMap.get(relType);
		Map<String, Relation> relMap = nodeRelation.get(relType);
		for (String endNodeId : relEndNodeIds) {
			com.ilimi.graph.dac.model.Node otherNode = existingNodes.get(endNodeId);
			if (null == otherNode) {
				otherNode = searchOps.getNodeByUniqueId(graphId, endNodeId, true, request);
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
	
	private void upsertRootNode(String graphId, Integer nodesCount, Integer relationsCount, Request request) {
		Neo4JBoltSearchOperations searchOps = new Neo4JBoltSearchOperations();
		Neo4JBoltNodeOperations nodeOps = new Neo4JBoltNodeOperations();
		String rootNodeUniqueId = Identifier.getIdentifier(graphId, SystemNodeTypes.ROOT_NODE.name());
		com.ilimi.graph.dac.model.Node node = searchOps.getNodeByUniqueId(graphId, rootNodeUniqueId, true, request);
		if (null == node) {
			node = new com.ilimi.graph.dac.model.Node();
			node.setGraphId(graphId);
			node.setIdentifier(rootNodeUniqueId);
			node.setMetadata(new HashMap<String, Object>());
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
		nodeOps.upsertNode(graphId, node, request);
	}

}
