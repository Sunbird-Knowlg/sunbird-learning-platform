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
import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;

public class Neo4JBoltSearchOperations {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedSearchOperations.class.getName());

	/**
	 * Gets the node by id.
	 *
	 * @param graphId
	 *            the graph id
	 * @param nodeId
	 *            the node id
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the node by id
	 */
	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Node Id: ", nodeId);
		LOGGER.debug("Get Tags ? ", getTags);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Id' Operation Failed.]");

		if (nodeId == 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_NODE_ID + " | ['Get Node By Id' Operation Failed.]");

		Node node = new Node();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
				parameterMap.put(GraphDACParams.getTags.name(), getTags);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.GET_NODE_BY_ID, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Node By Id' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Node By Id: ", node);
		return node;
	}

	/**
	 * Gets the node by unique id.
	 *
	 * @param graphId
	 *            the graph id
	 * @param nodeId
	 *            the node id
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the node by unique id
	 */
	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Node Id: ", nodeId);
		LOGGER.debug("Get Tags ? ", getTags);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Unique Id' Operation Failed.]");

		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | ['Get Node By Unique Id' Operation Failed.]");

		Node node = new Node();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
				parameterMap.put(GraphDACParams.getTags.name(), getTags);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.GET_NODE_BY_UNIQUE_ID, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Node By Unique Id' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Node By Unique Id: ", node);
		return node;
	}

	/**
	 * Gets the nodes by property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param property
	 *            the property
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the nodes by property
	 */
	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Property: ", property);
		LOGGER.debug("Get Tags ? ", getTags);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Nodes By Property' Operation Failed.]");

		if (null == property)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY + " | ['Get Nodes By Property' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.property.name(), property);
				parameterMap.put(GraphDACParams.getTags.name(), getTags);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.GET_NODES_BY_PROPERTY, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Node By Property' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Node By Property: ", nodes);
		return nodes;
	}

	/**
	 * Gets the node by unique ids.
	 *
	 * @param graphId
	 *            the graph id
	 * @param searchCriteria
	 *            the search criteria
	 * @param request
	 *            the request
	 * @return the node by unique ids
	 */
	public List<Node> getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Search Criteria: ", searchCriteria);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Get Nodes By Search Criteria' Operation Failed.]");

		if (null == searchCriteria)
			throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
					DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
							+ " | ['Get Nodes By Search Criteria' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.searchCriteria.name(), searchCriteria);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.GET_NODES_BY_SEARCH_CRITERIA, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Node By Search Criteria' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Node By Search Criteria: ", nodes);
		return nodes;
	}

	/**
	 * Gets the node property.
	 *
	 * @param graphId the graph id
	 * @param nodeId the node id
	 * @param key the key
	 * @param request the request
	 * @return the node property
	 */
	public Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Node Id: ", nodeId);
		LOGGER.debug("Property (Key): ", key);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node Property' Operation Failed.]");
		
		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | ['Get Node Property' Operation Failed.]");
		
		if (StringUtils.isBlank(key))
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY_KEY + " | ['Get Node Property' Operation Failed.]");
		

		Property property = new Property();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
				parameterMap.put(GraphDACParams.key.name(), key);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.GET_NODE_PROPERTY, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Node Property' Operation Finished.", record);
				}

			}
		}
		
		LOGGER.info("Returning Node Property: ", property);
		return property;
	}

	/**
	 * Gets the all nodes.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 * @return the all nodes
	 */
	public List<Node> getAllNodes(String graphId, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Get All Nodes' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.GET_ALL_NODES, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get All Nodes' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning All Nodes: ", nodes);
		return nodes;
	}

	/**
	 * Gets the all relations.
	 *
	 * @param graphId
	 *            the graph id
	 * @param request
	 *            the request
	 * @return the all relations
	 */
	public List<Relation> getAllRelations(String graphId, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Get All Relations' Operation Failed.]");

		List<Relation> relations = new ArrayList<Relation>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.GET_ALL_RELATIONS, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get All Relations' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning All Relations: ", relations);
		return relations;
	}

	/**
	 * Gets the relation property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param endNodeId
	 *            the end node id
	 * @param key
	 *            the key
	 * @param request
	 *            the request
	 * @return the relation property
	 */
	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Property (Key): ", key);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Relation Property' Operation Failed.]");
		
		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Get Relation Property' Operation Failed.]");
		
		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Get Relation Property' Operation Failed.]");
		
		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Get Relation Property' Operation Failed.]");
		
		if (StringUtils.isBlank(key))
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY_KEY + " | ['Get Relation Property' Operation Failed.]");
		

		Property property = new Property();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
				parameterMap.put(GraphDACParams.key.name(), key);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.GET_RELATION_PROPERTY, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Relation Property' Operation Finished.", record);
				}

			}
		}
		
		LOGGER.info("Returning Relation Property: ", property);
		return property;
	}

	/**
	 * Gets the relation.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param endNodeId
	 *            the end node id
	 * @param request
	 *            the request
	 * @return the relation
	 */
	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Relation' Operation Failed.]");
		
		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Get Relation' Operation Failed.]");
		
		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Get Relation' Operation Failed.]");
		
		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Get Relation' Operation Failed.]");
		
		Relation relation = new Relation();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.GET_RELATION, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Relation' Operation Finished.", record);
				}

			}
		}
		
		LOGGER.info("Returning Relation: ", relation);
		return relation;
	}

	/**
	 * Check cyclic loop.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param endNodeId
	 *            the end node id
	 * @param request
	 *            the request
	 * @return the map
	 */
	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("End Node Id: ", endNodeId);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Check Cyclic Loop' Operation Failed.]");
		
		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Check Cyclic Loop' Operation Failed.]");
		
		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE + " | ['Check Cyclic Loop' Operation Failed.]");
		
		if (StringUtils.isBlank(endNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Check Cyclic Loop' Operation Failed.]");
		
		Map<String, Object> cyclicLoopMap = new HashMap<String, Object>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.GET_RELATION, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Check Cyclic Loop' Operation Finished.", record);
				}

			}
		}
		
		LOGGER.info("Returning Cyclic Loop Map: ", cyclicLoopMap);
		return cyclicLoopMap;
	}

	/**
	 * Execute query.
	 *
	 * @param graphId
	 *            the graph id
	 * @param query
	 *            the query
	 * @param paramMap
	 *            the param map
	 * @param request
	 *            the request
	 * @return the list
	 */
	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Query: ", query);
		LOGGER.debug("Param Map: ", paramMap);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Execute Query' Operation Failed.]");
		
		if (StringUtils.isBlank(query))
			throw new ClientException(DACErrorCodeConstants.INVALID_QUERY.name(),
					DACErrorMessageConstants.INVALID_QUERY + " | ['Execute Query' Operation Failed.]");
		
		if (null == paramMap || paramMap.isEmpty())
			throw new ClientException(DACErrorCodeConstants.INVALID_PARAMETER.name(),
					DACErrorMessageConstants.INVALID_PARAM_MAP + " | ['Execute Query' Operation Failed.]");
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.cypherQuery.name(), query);
				parameterMap.put(GraphDACParams.paramMap.name(), paramMap);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.EXECUTE_QUERY, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Execute Query' Operation Finished.", record);
				}

			}
		}
		
		LOGGER.info("Returning Execute Query Result: ", resultList);
		return resultList;
	}

	/**
	 * Search nodes.
	 *
	 * @param graphId
	 *            the graph id
	 * @param searchCriteria
	 *            the search criteria
	 * @param getTags
	 *            the get tags
	 * @param request
	 *            the request
	 * @return the list
	 */
	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Search Criteria: ", searchCriteria);
		LOGGER.debug("Get Tags ? ", getTags);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Search Nodes' Operation Failed.]");

		if (null == searchCriteria)
			throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
					DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
							+ " | ['Search Nodes' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.searchCriteria.name(), searchCriteria);
				parameterMap.put(GraphDACParams.getTags.name(), getTags);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.SEARCH_NODES, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Search Nodes' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Search Nodes: ", nodes);
		return nodes;
	}

	/**
	 * Gets the nodes count.
	 *
	 * @param graphId
	 *            the graph id
	 * @param searchCriteria
	 *            the search criteria
	 * @param request
	 *            the request
	 * @return the nodes count
	 */
	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Search Criteria: ", searchCriteria);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Get Nodes Count' Operation Failed.]");

		if (null == searchCriteria)
			throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
					DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
							+ " | ['Get Nodes Count' Operation Failed.]");

		Long count = (long) 0;
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.searchCriteria.name(), searchCriteria);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.GET_NODES_COUNT, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Get Nodes Count' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Nodes Count: ", count);
		return count;
	}

	/**
	 * Traverse.
	 *
	 * @param graphId
	 *            the graph id
	 * @param traverser
	 *            the traverser
	 * @param request
	 *            the request
	 * @return the sub graph
	 */
	public SubGraph traverse(String graphId, Traverser traverser, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Traverser: ", traverser);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Traverse' Operation Failed.]");

		if (null == traverser)
			throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
					DACErrorMessageConstants.INVALID_TRAVERSER
							+ " | ['Traverse' Operation Failed.]");

		SubGraph subGraph = new SubGraph();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.traverser.name(), traverser);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.TRAVERSE, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Traverse' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Sub Graph: ", subGraph);
		return subGraph;
	}

	/**
	 * Traverse sub graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param traverser
	 *            the traverser
	 * @param request
	 *            the request
	 * @return the graph
	 */
	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Traverser: ", traverser);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Traverse Sub Graph' Operation Failed.]");

		if (null == traverser)
			throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
					DACErrorMessageConstants.INVALID_TRAVERSER
							+ " | ['Traverse Sub Graph' Operation Failed.]");

		Graph graph = new Graph();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.traverser.name(), traverser);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.TRAVERSE_SUB_GRAPH, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Traverse Sub Graph' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Graph : ", graph);
		return graph;
	}

	/**
	 * Gets the sub graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param startNodeId
	 *            the start node id
	 * @param relationType
	 *            the relation type
	 * @param depth
	 *            the depth
	 * @param request
	 *            the request
	 * @return the sub graph
	 */
	public Graph getSubGraph(String graphId, String startNodeId, String relationType, Integer depth, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Start Node Id: ", startNodeId);
		LOGGER.debug("Relation Type: ", relationType);
		LOGGER.debug("Depth: ", depth);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Get Sub Graph' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID
							+ " | ['Get Sub Graph' Operation Failed.]");
		
		if (StringUtils.isBlank(relationType))
			throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
					DACErrorMessageConstants.INVALID_RELATION_TYPE
							+ " | ['Get Sub Graph' Operation Failed.]");
		
		if (depth <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_DEPTH.name(),
					DACErrorMessageConstants.INVALID_DEPTH
							+ " | ['Get Sub Graph' Operation Failed.]");

		Graph graph = new Graph();
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				LOGGER.info("Populating Parameter Map.");
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
				parameterMap.put(GraphDACParams.relationType.name(), relationType);
				parameterMap.put(GraphDACParams.depth.name(), depth);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(QueryUtil.getQuery(Neo4JOperation.GET_SUB_GRAPH, parameterMap));
				for (Record record : result.list()) {
					LOGGER.debug("'Traverse Sub Graph' Operation Finished.", record);
				}

			}
		}

		LOGGER.info("Returning Graph Graph : ", graph);
		return graph;
	}

}
