package org.sunbird.graph.service.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.dac.model.Traverser;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.exceptions.ClientException;

public class SearchQueryGenerationUtil {

	public static String generateGetNodeByIdCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Id' Query Generation Failed.]");

			Long nodeId = (long) parameterMap.get(GraphDACParams.nodeId.name());
			if (nodeId == 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_NODE_ID + " | ['Get Node By Id' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + ") WHERE id(ee) = " + nodeId
					+ " OPTIONAL MATCH (ee)-[r]-() RETURN ee, r, startNode(r) as __startNode, endNode(r) as __endNode");

		}

		TelemetryManager.log("Returning Get Node By Id Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodeByUniqueIdCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Id' Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | ['Get Node By Unique Id' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + nodeId
					+ "'}) OPTIONAL MATCH (ee)-[r]-() RETURN ee, r, startNode(r) as __startNode, endNode(r) as __endNode");

		}

		TelemetryManager.log("Returning Get Node By Unique Id Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodesByPropertyCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Nodes By Property' Query Generation Failed.]");

			Property property = (Property) parameterMap.get(GraphDACParams.property.name());
			if (null == property)
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY
								+ " | ['Get Nodes By Property' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + " {" + property.getPropertyName() + ": '"
					+ property.getPropertyValue()
					+ "'}) OPTIONAL MATCH (ee)-[r]-() RETURN ee, r, startNode(r) as __startNode, endNode(r) as __endNode");
		}

		TelemetryManager.log("Returning Get Nodes By Property Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodeByUniqueIdsCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Nodes By Search Criteria' Query Generation Failed.]");

			SearchCriteria searchCriteria = (SearchCriteria) parameterMap.get(GraphDACParams.searchCriteria.name());
			if (null == searchCriteria)
				throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
						DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
								+ " | ['Get Nodes By Search Criteria' Query Generation Failed.]");

			searchCriteria.setGraphId(graphId);
			query.append(searchCriteria.getQuery());
		}

		TelemetryManager.log("Returning Get Node By Unique Ids Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodePropertyCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Node Property' Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | ['Get Node Property' Query Generation Failed.]");

			String key = (String) parameterMap.get(GraphDACParams.key.name());
			if (StringUtils.isBlank(key))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY_KEY
								+ " | ['Get Node Property' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + nodeId
					+ "'}) OPTIONAL MATCH (ee)-[r]-() RETURN ee." + key + " as " + key + "");
		}

		TelemetryManager.log("Returning Get Node Property Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetAllNodesCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get All Nodes' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId
					+ ") OPTIONAL MATCH (ee)-[r]-() RETURN ee, r, startNode(r) as __startNode, endNode(r) as __endNode");
		}

		TelemetryManager.log("Returning Get All Nodes Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetAllRelationsCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get All Relations' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + ")-[r]-(aa:" + graphId
					+ ") RETURN r, startNode(r) as __startNode, endNode(r) as __endNode");
		}

		TelemetryManager.log("Returning Get All Relations Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetRelationPropertyCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String key = (String) parameterMap.get(GraphDACParams.key.name());
			if (StringUtils.isBlank(key))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY_KEY
								+ " | ['Get Relation Property' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
					+ "'})-[r:" + relationType + "]-(aa:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name()
					+ ": '" + endNodeId + "'}) RETURN r." + key + " as " + key + "");
		}

		TelemetryManager.log("Returning Get Relation Property Cypher Query: " + query);
		return query.toString();
	}
	
	public static String generateGetRelationByIdCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Relation' Query Generation Failed.]");

			Long id = (long) parameterMap.get(GraphDACParams.identifier.name());
			if (null == id || id < 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | ['Get Relation By Id' Query Generation Failed.]");

			query.append("MATCH ()-[r]-() where ID(r)= " + id + " RETURN r, startNode(r) as __startNode, endNode(r) as __endNode");
		}

		TelemetryManager.log("Returning Get Relation By Id Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetRelationCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Relation' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Get Relation' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Get Relation' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID + " | ['Get Relation' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
					+ "'})-[r:" + relationType + "]-(aa:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name()
					+ ": '" + endNodeId + "'}) RETURN r, startNode(r) as __startNode, endNode(r) as __endNode");

		}

		TelemetryManager.log("Returning Get Relation Cypher Query: " + query);
		return query.toString();
	}

	public static String generateCheckCyclicLoopCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

			query.append("MATCH (ee:" + graphId + " { " + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
					+ "' })-[:" + relationType + "*1..]->(aa:" + graphId + "{" + SystemProperties.IL_UNIQUE_ID.name()
					+ ": '" + endNodeId + "'}) RETURN aa");

		}

		TelemetryManager.log("Returning Check Cyclic Loop Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateExecuteQueryCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Execute Query' Query Generation Failed.]");

			String cypherQuery = (String) parameterMap.get(GraphDACParams.cypherQuery.name());
			if (StringUtils.isBlank(cypherQuery))
				throw new ClientException(DACErrorCodeConstants.INVALID_QUERY.name(),
						DACErrorMessageConstants.INVALID_QUERY + " | ['Execute Query' Query Generation Failed.]");

			Map<String, Object> paramMap = (Map<String, Object>) parameterMap.get(GraphDACParams.paramMap.name());
			if (null == paramMap || paramMap.isEmpty())
				throw new ClientException(DACErrorCodeConstants.INVALID_PARAMETER.name(),
						DACErrorMessageConstants.INVALID_PARAM_MAP + " | ['Execute Query' Query Generation Failed.]");
			query.append(cypherQuery);

		}

		TelemetryManager.log("Returning Execute Query, Cypher Query: " + query);
		return query.toString();
	}

	public static String generateSearchNodesCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Search Nodes' Query Generation Failed.]");

			SearchCriteria searchCriteria = (SearchCriteria) parameterMap.get(GraphDACParams.searchCriteria.name());
			if (null == searchCriteria)
				throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
						DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
								+ " | ['Search Nodes' Query Generation Failed.]");

			searchCriteria.setGraphId(graphId);
			String cypherQuery = searchCriteria.getQuery();
			query.append(cypherQuery);
		}

		TelemetryManager.log("Returning search Nodes Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodesCountCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Nodes Count' Query Generation Failed.]");

			SearchCriteria searchCriteria = (SearchCriteria) parameterMap.get(GraphDACParams.searchCriteria.name());
			if (null == searchCriteria)
				throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
						DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
								+ " | ['Nodes Count' Query Generation Failed.]");
			searchCriteria.setGraphId(graphId);
			query.append(searchCriteria.getQuery());
		}

		TelemetryManager.log("Returning Nodes Count Cypher Query: " + query);
		return query.toString();
	}

	public static String generateTraverseCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Traverse' Query Generation Failed.]");

			Traverser traverser = (Traverser) parameterMap.get(GraphDACParams.traverser.name());
			if (null == traverser)
				throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
						DACErrorMessageConstants.INVALID_TRAVERSER + " | ['Traverse' Query Generation Failed.]");
		}

		TelemetryManager.log("Returning Traverse Cypher Query: " + query);
		return query.toString();
	}

	public static String generateTraverseSubGraphCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Traverse Sub Graph' Query Generation Failed.]");

			Traverser traverser = (Traverser) parameterMap.get(GraphDACParams.traverser.name());
			if (null == traverser)
				throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
						DACErrorMessageConstants.INVALID_TRAVERSER
								+ " | ['Traverse Sub Graph' Query Generation Failed.]");
		}

		TelemetryManager.log("Returning Traverse Sub Graph Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetSubGraphCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Sub Graph' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Get Sub Graph' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Get Sub Graph' Query Generation Failed.]");

			int depth = (int) parameterMap.get(GraphDACParams.graphId.name());
			if (depth <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_DEPTH.name(),
						DACErrorMessageConstants.INVALID_DEPTH + " | ['Get Sub Graph' Query Generation Failed.]");
		}

		TelemetryManager.log("Returning Get Sub Graph Cypher Query: " + query);
		return query.toString();
	}

}
