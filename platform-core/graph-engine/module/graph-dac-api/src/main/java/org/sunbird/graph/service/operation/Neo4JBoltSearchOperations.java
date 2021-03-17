package org.sunbird.graph.service.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.graph.cache.mgr.impl.NodeCacheManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Graph;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.dac.model.SubGraph;
import org.sunbird.graph.dac.model.Traverser;
import org.sunbird.graph.service.common.CypherQueryConfigurationConstants;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.graph.service.common.GraphOperation;
import org.sunbird.graph.service.util.DriverUtil;
import org.sunbird.graph.service.util.SearchQueryGenerationUtil;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.graphdb.Direction;

public class Neo4JBoltSearchOperations {

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
	public static Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Id' Operation Failed.]");

		if (nodeId == 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_NODE_ID + " | ['Get Node By Id' Operation Failed.]");

		Node node = new Node();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
			parameterMap.put(GraphDACParams.getTags.name(), getTags);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetNodeByIdCypherQuery(parameterMap));
			if (null == result || !result.hasNext())
				throw new ResourceNotFoundException(DACErrorCodeConstants.NOT_FOUND.name(),
						DACErrorMessageConstants.NODE_NOT_FOUND + " | [Invalid Node Id.]");

			Map<Long, Object> nodeMap = new HashMap<Long, Object>();
			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			for (Record record : result.list()) {
				TelemetryManager.log("'Get Node By Id' Operation Finished.", record.asMap());
				if (null != record)
					getRecordValues(record, nodeMap, relationMap, startNodeMap, endNodeMap);
			}

			if (!nodeMap.isEmpty()) {
				for (Entry<Long, Object> entry : nodeMap.entrySet())
					node = new Node(graphId, (org.neo4j.driver.v1.types.Node) entry.getValue(), relationMap,
							startNodeMap, endNodeMap);
			}
		}
		TelemetryManager.log("Returning Node By Id: ", node.getMetadata());
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
	public static Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		TelemetryManager.log("Graph Id: " + graphId + "\nNode Id: " + nodeId + "\nGet Tags:" + getTags);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Unique Id' Operation Failed.]");

		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | ['Get Node By Unique Id' Operation Failed.]");

		
		Node node = (Node) NodeCacheManager.getDataNode(graphId, nodeId);
		if (null != node) {
			TelemetryManager.info("Fetched node from in-memory cache: "+node.getIdentifier());
			return node;
		} else {
			Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
			TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put(GraphDACParams.graphId.name(), graphId);
				parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
				parameterMap.put(GraphDACParams.getTags.name(), getTags);
				parameterMap.put(GraphDACParams.request.name(), request);

				StatementResult result = session
						.run(SearchQueryGenerationUtil.generateGetNodeByUniqueIdCypherQuery(parameterMap));
				if (null == result || !result.hasNext())
					throw new ResourceNotFoundException(DACErrorCodeConstants.NOT_FOUND.name(),
							DACErrorMessageConstants.NODE_NOT_FOUND + " | [Invalid Node Id.]: " + nodeId, nodeId);

				Map<Long, Object> nodeMap = new HashMap<Long, Object>();
				Map<Long, Object> relationMap = new HashMap<Long, Object>();
				Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
				Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
				for (Record record : result.list()) {
					TelemetryManager.log("'Get Node By Unique Id' Operation Finished.", record.asMap());
					if (null != record)
						getRecordValues(record, nodeMap, relationMap, startNodeMap, endNodeMap);
				}

				if (!nodeMap.isEmpty()) {
					for (Entry<Long, Object> entry : nodeMap.entrySet())
						node = new Node(graphId, (org.neo4j.driver.v1.types.Node) entry.getValue(), relationMap,
								startNodeMap, endNodeMap);
				}
			}
			return node;
		}
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
	public static List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		TelemetryManager.log("Graph Id: " + graphId + "\nProperty: " + property + "\nGet Tags:" + getTags);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Nodes By Property' Operation Failed.]");

		if (null == property)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY + " | ['Get Nodes By Property' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.property.name(), property);
			parameterMap.put(GraphDACParams.getTags.name(), getTags);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetNodesByPropertyCypherQuery(parameterMap));
			Map<Long, Object> nodeMap = new HashMap<Long, Object>();
			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get Nodes By Property Id' Operation Finished.", record.asMap());
					if (null != record)
						getRecordValues(record, nodeMap, relationMap, startNodeMap, endNodeMap);
				}
			}

			if (!nodeMap.isEmpty()) {
				for (Entry<Long, Object> entry : nodeMap.entrySet())
					nodes.add(new Node(graphId, (org.neo4j.driver.v1.types.Node) entry.getValue(), relationMap,
							startNodeMap, endNodeMap));
			}
		}
		TelemetryManager.log("Returning Node By Property: " + nodes.size());
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
	public static List<Node> getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID
							+ " | ['Get Nodes By Search Criteria' Operation Failed.]");

		if (null == searchCriteria)
			throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
					DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
							+ " | ['Get Nodes By Search Criteria' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.searchCriteria.name(), searchCriteria);
			parameterMap.put(GraphDACParams.request.name(), request);

			String query = SearchQueryGenerationUtil.generateGetNodeByUniqueIdsCypherQuery(parameterMap);
			Map<String, Object> params = searchCriteria.getParams();
			StatementResult result = session.run(query, params);
			Map<Long, Object> nodeMap = new HashMap<Long, Object>();
			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get Nodes By Search Criteria' Operation Finished.", record.asMap());
					if (null != record)
						getRecordValues(record, nodeMap, relationMap, startNodeMap, endNodeMap);
				}
			}

			if (!nodeMap.isEmpty()) {
				for (Entry<Long, Object> entry : nodeMap.entrySet())
					nodes.add(new Node(graphId, (org.neo4j.driver.v1.types.Node) entry.getValue(), relationMap,
							startNodeMap, endNodeMap));
			}
		}
		TelemetryManager.log("Returning Node By Search Criteria: " + nodes.size());
		return nodes;
	}

	public static List<Map<String, Object>> executeQueryForProps(String graphId, String query, List<String> propKeys) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(), DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Execute Query For Nodes' Operation Failed.]");
		List<Map<String, Object>> propsList = new ArrayList<Map<String, Object>>();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {

			StatementResult result = session.run(query);
			if (null != result) {
				for (Record record : result.list()) {
					if (null != record) {
						Map<String, Object> row = new HashMap<String, Object>();
						for (int i = 0; i < propKeys.size(); i++) {
							String key = propKeys.get(i);
							Value value = record.get(key);
							if (null != value) 
								row.put(key, value.asObject());
						}
						if (!row.isEmpty())
							propsList.add(row);
					}
				}
			}
		}
		return propsList;
	}
	
	/**
	 * Gets the node property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param nodeId
	 *            the node id
	 * @param key
	 *            the key
	 * @param request
	 *            the request
	 * @return the node property
	 */
	public static Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		TelemetryManager.log("Graph Id: " + graphId + "\nNode Id: " + nodeId + "\nProperty (Key): " + key);


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
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
			parameterMap.put(GraphDACParams.key.name(), key);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetNodePropertyCypherQuery(parameterMap));
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get Node Property' Operation Finished.", record.asMap());
					if (null != record && null != record.get(key)) {
						property.setPropertyName(key);
						property.setPropertyValue(record.get(key));
					}
				}
			}
		}
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
	public static List<Node> getAllNodes(String graphId, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get All Nodes' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetAllNodesCypherQuery(parameterMap));
			Map<Long, Object> nodeMap = new HashMap<Long, Object>();
			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get All Nodes' Operation Finished.", record.asMap());
					if (null != record)
						getRecordValues(record, nodeMap, relationMap, startNodeMap, endNodeMap);
				}
			}
			
			if (!nodeMap.isEmpty()) {
				for (Entry<Long, Object> entry : nodeMap.entrySet())
					nodes.add(new Node(graphId, (org.neo4j.driver.v1.types.Node) entry.getValue(), relationMap,
							startNodeMap, endNodeMap));
			}
		}
		TelemetryManager.log("Returning All Nodes: " + nodes.size());
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
	public static List<Relation> getAllRelations(String graphId, Request request) {
		
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get All Relations' Operation Failed.]");

		List<Relation> relations = new ArrayList<Relation>();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetAllRelationsCypherQuery(parameterMap));
			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get All Relations' Operation Finished.", record.asMap());
					if (null != record)
						getRecordValues(record, null, relationMap, startNodeMap, endNodeMap);
				}
			}
			TelemetryManager.log("Relation Map: " + relationMap + "\nStart Node Map: " + startNodeMap + "\nEnd Node Map: "
					+ endNodeMap);

			if (!relationMap.isEmpty()) {
				for (Entry<Long, Object> entry : relationMap.entrySet())
					relations.add(new Relation(graphId, (org.neo4j.driver.v1.types.Relationship) entry.getValue(),
							startNodeMap, endNodeMap));
			}
		}
		TelemetryManager.log("Returning All Relations: " + relations.size());
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
	public static Property getRelationProperty(String graphId, String startNodeId, String relationType,
			String endNodeId,
			String key, Request request) {
		TelemetryManager.log("Graph Id: " + graphId + "\nStart Node Id: " + startNodeId + "\nRelation Type: "
				+ relationType + "\nEnd Node Id: " + endNodeId + "\nProperty (Key): " + key);


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
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.key.name(), key);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetRelationPropertyCypherQuery(parameterMap));
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get Relation Property' Operation Finished.", record.asMap());
					if (null != record && null != record.get(key)) {
						property.setPropertyName(key);
						property.setPropertyValue(record.get(key));
					}
				}
			}
		}
		return property;
	}
	
	public static Relation getRelationById(String graphId, Long relationId, Request request) {
		TelemetryManager.log("Graph Id: " + graphId + "\nRelation Id: " + relationId);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Relation By Id' Operation Failed.]");

		if (null == relationId || relationId < 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | ['Get Relation' Operation Failed.]");
		
		Relation relation = new Relation();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.identifier.name(), relationId);
			parameterMap.put(GraphDACParams.request.name(), request);
			
			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetRelationByIdCypherQuery(parameterMap));
			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get Relation' Operation Finished.", record.asMap());
					if (null != record)
						getRecordValues(record, null, relationMap, startNodeMap, endNodeMap);
				}
			}
			TelemetryManager.log("Relation Map: " + relationMap + "\nStart Node Map: " + startNodeMap + "\nEnd Node Map: "
					+ endNodeMap);

			if (!relationMap.isEmpty()) {
				for (Entry<Long, Object> entry : relationMap.entrySet())
					relation = new Relation(graphId, (org.neo4j.driver.v1.types.Relationship) entry.getValue(),
							startNodeMap, endNodeMap);
			}
		}
		return relation;
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
	public static Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {

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
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.request.name(), request);
			
			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateGetRelationCypherQuery(parameterMap));
			if (null == result || !result.hasNext())
				throw new ResourceNotFoundException(DACErrorCodeConstants.NOT_FOUND.name(),
						DACErrorMessageConstants.NODE_NOT_FOUND + " | [No Relation found.]");

			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			for (Record record : result.list()) {
				TelemetryManager.log("'Get Relation' Operation Finished.", record.asMap());
				if (null != record)
					getRecordValues(record, null, relationMap, startNodeMap, endNodeMap);
			}
			TelemetryManager.log("Relation Map: " + relationMap + "\nStart Node Map: " + startNodeMap + "\nEnd Node Map: "
					+ endNodeMap);

			if (!relationMap.isEmpty()) {
				for (Entry<Long, Object> entry : relationMap.entrySet())
					relation = new Relation(graphId, (org.neo4j.driver.v1.types.Relationship) entry.getValue(),
							startNodeMap, endNodeMap);
			}
		}
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
	public static Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		TelemetryManager.log("Graph Id: " + graphId + "\nStart Node Id: " + startNodeId + "\nRelation Type: "
				+ relationType + "\nEnd Node Id: " + endNodeId);


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
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.startNodeId.name(), startNodeId);
			parameterMap.put(GraphDACParams.relationType.name(), relationType);
			parameterMap.put(GraphDACParams.endNodeId.name(), endNodeId);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session
					.run(SearchQueryGenerationUtil.generateCheckCyclicLoopCypherQuery(parameterMap));
			if (null != result && result.hasNext()) {
				cyclicLoopMap.put(GraphDACParams.loop.name(), new Boolean(true));
				cyclicLoopMap.put(GraphDACParams.message.name(),
						startNodeId + " and " + endNodeId + " are connected by relation: " + relationType);
			} else {
				cyclicLoopMap.put(GraphDACParams.loop.name(), new Boolean(false));
			}
		}

		TelemetryManager.log("Returning Cyclic Loop Map: ", cyclicLoopMap);
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
	public static List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		TelemetryManager.log("Graph Id: " + graphId + "\nQuery: " + query + "\nParam Map: ", paramMap);

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
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.cypherQuery.name(), query);
			parameterMap.put(GraphDACParams.paramMap.name(), paramMap);
			parameterMap.put(GraphDACParams.request.name(), request);

			StatementResult result = session.run(SearchQueryGenerationUtil.generateExecuteQueryCypherQuery(parameterMap),
					paramMap);
			for (Record record : result.list()) {
				TelemetryManager.log("'Execute Query' Operation Finished.", record.asMap());
				Map<String, Object> recordMap = record.asMap();
				Map<String, Object> map = new HashMap<String, Object>();
				if (null != recordMap && !recordMap.isEmpty()) {
					for (Entry<String, Object> entry : recordMap.entrySet()) {
						map.put(entry.getKey(), entry.getValue());
					}
					resultList.add(map);
				}
			}
		}
		TelemetryManager.log("Returning Execute Query Result: "+ resultList.size());
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
	public static List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags,
			Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Search Nodes' Operation Failed.]");

		if (null == searchCriteria)
			throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
					DACErrorMessageConstants.INVALID_SEARCH_CRITERIA + " | ['Search Nodes' Operation Failed.]");

		List<Node> nodes = new ArrayList<Node>();
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
			List<String> fields = searchCriteria.getFields();
			boolean returnNode = true;
			if (null != fields && !fields.isEmpty())
				returnNode = false;
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.searchCriteria.name(), searchCriteria);
			parameterMap.put(GraphDACParams.getTags.name(), getTags);
			parameterMap.put(GraphDACParams.request.name(), request);

			String query = SearchQueryGenerationUtil.generateSearchNodesCypherQuery(parameterMap);
			TelemetryManager.log("Search Query: " + query);
			Map<String, Object> params = searchCriteria.getParams();
			TelemetryManager.log("Search Params: " + params);
			StatementResult result = session.run(query, params);
			Map<Long, Object> nodeMap = new LinkedHashMap<Long, Object>();
			Map<Long, Object> relationMap = new HashMap<Long, Object>();
			Map<Long, Object> startNodeMap = new HashMap<Long, Object>();
			Map<Long, Object> endNodeMap = new HashMap<Long, Object>();
			if (null != result) {
				TelemetryManager.log("'Search Nodes' result: " + result);
				for (Record record : result.list()) {
					TelemetryManager.log("'Search Nodes' Operation Finished.", record.asMap());
					if (null != record) {
						if (returnNode)
							getRecordValues(record, nodeMap, relationMap, startNodeMap, endNodeMap);
						else {
							Node node = new Node(graphId, record.asMap());
							nodes.add(node);
						}
					}
				}
			}
			TelemetryManager.log("Node Map: " + nodeMap + "\nRelation Map: " + relationMap + "\nStart Node Map: "
					+ startNodeMap + "\nEnd Node Map: " + endNodeMap);

			if (!nodeMap.isEmpty()) {
				for (Entry<Long, Object> entry : nodeMap.entrySet())
					nodes.add(new Node(graphId, (org.neo4j.driver.v1.types.Node) entry.getValue(), relationMap,
							startNodeMap, endNodeMap));
			}
		}
		TelemetryManager.log("Returning Search Nodes: " + nodes);
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
	public static Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Nodes Count' Operation Failed.]");

		if (null == searchCriteria)
			throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
					DACErrorMessageConstants.INVALID_SEARCH_CRITERIA + " | ['Get Nodes Count' Operation Failed.]");

		Long count = (long) 0;
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");

			searchCriteria.setCountQuery(true);
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.searchCriteria.name(), searchCriteria);
			parameterMap.put(GraphDACParams.request.name(), request);

			String query = SearchQueryGenerationUtil.generateGetNodesCountCypherQuery(parameterMap);
			Map<String, Object> params = searchCriteria.getParams();
			StatementResult result = session.run(query, params);
			if (null != result) {
				for (Record record : result.list()) {
					TelemetryManager.log("'Get Nodes Count' Operation Finished.", record.asMap());
					if (null != record && null != record.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_COUNT_OBJECT))
						count = record.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_COUNT_OBJECT).asLong();
				}
			}
		}
		TelemetryManager.log("Returning Nodes Count: " + count);
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
	public static SubGraph traverse(String graphId, Traverser traverser, Request request) {
		
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Traverse' Operation Failed.]");

		if (null == traverser)
			throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
					DACErrorMessageConstants.INVALID_TRAVERSER + " | ['Traverse' Operation Failed.]");

		SubGraph subGraph = traverser.traverse();
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
	public static Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Traverse Sub Graph' Operation Failed.]");

		if (null == traverser)
			throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
					DACErrorMessageConstants.INVALID_TRAVERSER + " | ['Traverse Sub Graph' Operation Failed.]");

		Graph subGraph = traverser.getSubGraph();
		return subGraph;
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
	public static Graph getSubGraph(String graphId, String startNodeId, String relationType, Integer depth,
			Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Sub Graph' Operation Failed.]");

		if (StringUtils.isBlank(startNodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_START_NODE_ID + " | ['Get Sub Graph' Operation Failed.]");

		Traverser traverser = new Traverser(graphId, startNodeId);
		traverser = traverser.addRelationMap(relationType, Direction.OUTGOING.name());
		if (null != depth && depth.intValue() > 0) {
			traverser.toDepth(depth);
		}
		Graph subGraph = traverser.getSubGraph();
		return subGraph;
	}

	private static void getRecordValues(Record record, Map<Long, Object> nodeMap, Map<Long, Object> relationMap,
			Map<Long, Object> startNodeMap, Map<Long, Object> endNodeMap) {
		if (null != nodeMap) {
			Value nodeValue = record.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT);
			if (null != nodeValue && StringUtils.equalsIgnoreCase("NODE", nodeValue.type().name())) {
				org.neo4j.driver.v1.types.Node neo4jBoltNode = record
						.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT).asNode();
				nodeMap.put(neo4jBoltNode.id(), neo4jBoltNode);
			}
		}
		if (null != relationMap) {
			Value relValue = record.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_RELATION_OBJECT);
			if (null != relValue && StringUtils.equalsIgnoreCase("RELATIONSHIP", relValue.type().name())) {
				org.neo4j.driver.v1.types.Relationship relationship = record
						.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_RELATION_OBJECT).asRelationship();
				relationMap.put(relationship.id(), relationship);
			}
		}
		if (null != startNodeMap) {
			Value startNodeValue = record.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_START_NODE_OBJECT);
			if (null != startNodeValue && StringUtils.equalsIgnoreCase("NODE", startNodeValue.type().name())) {
				org.neo4j.driver.v1.types.Node startNode = record
						.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_START_NODE_OBJECT).asNode();
				startNodeMap.put(startNode.id(), startNode);
			}
		}
		if (null != endNodeMap) {
			Value endNodeValue = record.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_END_NODE_OBJECT);
			if (null != endNodeValue && StringUtils.equalsIgnoreCase("NODE", endNodeValue.type().name())) {
				org.neo4j.driver.v1.types.Node endNode = record
						.get(CypherQueryConfigurationConstants.DEFAULT_CYPHER_END_NODE_OBJECT).asNode();
				endNodeMap.put(endNode.id(), endNode);
			}
		}
	}

}
