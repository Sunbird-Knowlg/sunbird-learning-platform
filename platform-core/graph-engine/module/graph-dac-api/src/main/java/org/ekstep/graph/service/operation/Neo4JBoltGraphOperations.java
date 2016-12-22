package org.ekstep.graph.service.operation;

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
import com.ilimi.graph.dac.enums.GraphDACParams;
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
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Graph Unique Contraint' Operation Failed.]");

		if (null == indexProperties || indexProperties.size() > 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
							+ " | ['Create Graph Unique Contraint' Operation Failed.]");

		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.indexProperties.name(), indexProperties);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.CREATE_UNIQUE_CONSTRAINT, parameterMap));
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

		if (null == indexProperties || indexProperties.size() > 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
							+ " | [Create Graph Index Operation Failed.]");

		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.indexProperties.name(), indexProperties);
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

		try (Driver driver = DriverUtil.getDriver(graphId)) {
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


		try (Driver driver = DriverUtil.getDriver(graphId)) {
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


		try (Driver driver = DriverUtil.getDriver(graphId)) {
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

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.UPDATE_RELATION, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Update Relation' Operation Finished.", record);
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


		try (Driver driver = DriverUtil.getDriver(graphId)) {
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
					DACErrorMessageConstants.INVALID_START_NODE_ID_LIST + " | ['Create Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Create Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Create Incoming Relations' Operation Failed.]");


		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeIds.name(), startNodeIds);
				parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.CREATE_INCOMING_RELATIONS, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Create Incoming Relations' Operation Finished.", record);
				}

			}
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
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Create Outgoing Relations' Operation Failed.]");

		if (null == endNodeIds || endNodeIds.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID_LIST + " | ['Create Outgoing Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Create Outgoing Relations' Operation Failed.]");


		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.endNodeIds.name(), endNodeIds);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.CREATE_OUTGOING_RELATIONS, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Create Outgoing Relations' Operation Finished.", record);
				}

			}
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
					DACErrorMessageConstants.INVALID_START_NODE_ID_LIST + " | ['Delete Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Delete Incoming Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Delete Incoming Relations' Operation Failed.]");


		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeIds.name(), startNodeIds);
				parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.DELETE_INCOMING_RELATIONS, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Delete Incoming Relations' Operation Finished.", record);
				}

			}
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
					DACErrorMessageConstants.INVALID_START_NODE_ID_LIST + " | ['Delete Outgoing Relations' Operation Failed.]");

		if (null == endNodeIds || endNodeIds.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Delete Outgoing Relations' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Delete Outgoing Relations' Operation Failed.]");


		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.endNodeIds.name(), endNodeIds);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.DELETE_OUTGOING_RELATIONS, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Delete Outgoing Relations' Operation Finished.", record);
				}

			}
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
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Remove Relation Metadata' Operation Failed.]");

		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Remove Relation Metadata' Operation Failed.]");

		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Remove Relation Metadata' Operation Failed.]");
		
		if (StringUtils.isBlank(key))
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY_KEY + " | ['Remove Relation Metadata' Operation Failed.]");


		try (Driver driver = DriverUtil.getDriver(graphId)) {
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

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.REMOVE_RELATION_METADATA, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Remove Relation Metadata' Operation Finished.", record);
				}

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
		
		if (StringUtils.isBlank(collectionId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_COLLECTION_NODE_ID + " | ['Create Collection' Operation Failed.]");

		if (null == collection)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_COLLECTION_NODE + " | ['Create Collection' Operation Failed.]");
		
		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Create Collection' Operation Failed.]");
		
		if (null == members || members.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_MEMBERS.name(),
					DACErrorMessageConstants.INVALID_COLLECTION_MEMBERS + " | ['Create Collection' Operation Failed.]");
		
		if (StringUtils.isBlank(indexProperty))
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_INDEX_PROPERTY + " | ['Create Collection' Operation Failed.]");


		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.collectionId.name(), collectionId);
				parameterMap.put(GraphDACParams.collection.name(), collection);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.members.name(), members);
				parameterMap.put(GraphDACParams.indexProperty.name(), indexProperty);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.CREATE_COLLECTION, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Create Collection' Operation Finished.", record);
				}

			}
		}
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

		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.collectionId.name(), collectionId);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.DELETE_COLLECTION, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Delete Collection' Operation Finished.", record);
				}

			}
		}
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
		
		if (StringUtils.isBlank(taskId))
			throw new ClientException(DACErrorCodeConstants.INVALID_TASK.name(),
					DACErrorMessageConstants.INVALID_TASK_ID + " | ['Import Graph' Operation Failed.]");
		
		if (null == input)
			throw new ClientException(DACErrorCodeConstants.INVALID_DATA.name(),
					DACErrorMessageConstants.INVALID_IMPORT_DATA + " | ['Import Graph' Operation Failed.]");

		Map<String, List<String>> messages = new HashMap<String, List<String>>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.taskId.name(), taskId);
				parameterMap.put(GraphDACParams.input.name(), input);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.IMPORT_GRAPH, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Import Graph' Operation Finished.", record);
				}

			}
		}
		
		return messages;
	}

}
