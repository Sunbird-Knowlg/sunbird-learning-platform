package org.sunbird.graph.service.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.common.CypherQueryConfigurationConstants;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.exceptions.ClientException;

public class NodeQueryGenerationUtil extends BaseQueryGenerationUtil {

	@SuppressWarnings("unchecked")
	public static String generateCreateNodeCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Create Node' Query Generation Failed.]");

			Node node = (Node) parameterMap.get(GraphDACParams.node.name());
			if (null == node)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE + " | [Create Node Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();

			Map<String, Object> queryMap = new HashMap<String, Object>();
			Map<String, Object> templateQueryMap = new HashMap<String, Object>();
			StringBuilder templateQuery = new StringBuilder();
			Map<String, Object> templateParamValueMap = new HashMap<String, Object>();

			// Template Query
			Map<String, Object> mpMap = getMetadataCypherQueryMap(node);
			Map<String, Object> spMap = getSystemPropertyQueryMap(node, date);
			Map<String, Object> apMap = getAuditPropertyQueryMap(node, date, false);
			Map<String, Object> vpMap = getVersionKeyPropertyQueryMap(node, date, false);
			templateQuery.append(GraphDACParams.CREATE.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(mpMap.get(GraphDACParams.query.name())).append(CypherQueryConfigurationConstants.COMMA)
					.append(spMap.get(GraphDACParams.query.name())).append(CypherQueryConfigurationConstants.COMMA)
					.append(apMap.get(GraphDACParams.query.name())).append(CypherQueryConfigurationConstants.COMMA)
					.append(vpMap.get(GraphDACParams.query.name()))
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS);

			// Return Node
			templateQuery.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT);
			templateParamValueMap.putAll((Map<String, Object>) mpMap.get(GraphDACParams.paramValueMap.name()));
			templateParamValueMap.putAll((Map<String, Object>) spMap.get(GraphDACParams.paramValueMap.name()));
			templateParamValueMap.putAll((Map<String, Object>) apMap.get(GraphDACParams.paramValueMap.name()));
			templateParamValueMap.putAll((Map<String, Object>) vpMap.get(GraphDACParams.paramValueMap.name()));

			templateQueryMap.put(GraphDACParams.query.name(), templateQuery.toString());
			templateQueryMap.put(GraphDACParams.paramValueMap.name(), templateParamValueMap);

			queryMap.put(node.getIdentifier(), templateQueryMap);

			parameterMap.put(GraphDACParams.queryStatementMap.name(), queryMap);

		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateUpsertNodeCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			Node node = (Node) parameterMap.get(GraphDACParams.node.name());
			if (null == node)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE + " | [Create Node Query Generation Failed.]");
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), Identifier.getUniqueIdFromTimestamp()));
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			String date = DateUtils.formatCurrentDate();
			
			Map<String, Object> queryMap = new HashMap<String, Object>();
			Map<String, Object> templateQueryMap = new HashMap<String, Object>();
			StringBuilder templateQuery = new StringBuilder();
			Map<String, Object> templateParamValueMap = new HashMap<String, Object>();
			// Template Query
			Map<String, Object> ocsMap = getOnCreateSetQueryMap("ee", date, node);
			Map<String, Object> omsMap = getOnMatchSetQueryMap("ee", date, node, true);
			templateQuery.append(GraphDACParams.MERGE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS)
					.append("ee" + CypherQueryConfigurationConstants.COLON).append(graphId)
					.append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getMatchCriteriaString(graphId, node))
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(ocsMap.get(GraphDACParams.query.name()))
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(omsMap.get(GraphDACParams.query.name()));

			// Return Node
			templateQuery.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append("ee");
			templateParamValueMap.putAll((Map<String, Object>) ocsMap.get(GraphDACParams.paramValueMap.name()));
			templateParamValueMap.putAll((Map<String, Object>) omsMap.get(GraphDACParams.paramValueMap.name()));
			
			TelemetryManager.log("Returning Upsert Node Cypher Query: " + templateQuery);

			templateQueryMap.put(GraphDACParams.query.name(), templateQuery.toString());
			templateQueryMap.put(GraphDACParams.paramValueMap.name(), templateParamValueMap);

			queryMap.put(node.getIdentifier(), templateQueryMap);
			
			parameterMap.put(GraphDACParams.queryStatementMap.name(), queryMap);
		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateUpdateNodeCypherQuery(Map<String, Object> parameterMap) {

		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Update Node' Query Generation Failed.]");

			Node node = (Node) parameterMap.get(GraphDACParams.node.name());
			if (null == node)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE + " | [Create Node Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();

			Map<String, Object> queryMap = new HashMap<String, Object>();
			Map<String, Object> templateQueryMap = new HashMap<String, Object>();
			StringBuilder templateQuery = new StringBuilder();
			Map<String, Object> templateParamValueMap = new HashMap<String, Object>();
			// Template Query
			Map<String, Object> omsMap = getOnMatchSetQueryMap("ee", date, node, false);
			templateQuery.append(GraphDACParams.MATCH.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS)
					.append("ee" + CypherQueryConfigurationConstants.COLON).append(graphId)
					.append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getMatchCriteriaString(graphId, node))
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(omsMap.get(GraphDACParams.query.name()));

			// Return Node
			templateQuery.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append("ee");
			templateParamValueMap.putAll((Map<String, Object>) omsMap.get(GraphDACParams.paramValueMap.name()));
			
			TelemetryManager.log("Returning Update Node Cypher Query: " + templateQuery);

			templateQueryMap.put(GraphDACParams.query.name(), templateQuery.toString());
			templateQueryMap.put(GraphDACParams.paramValueMap.name(), templateParamValueMap);

			queryMap.put(node.getIdentifier(), templateQueryMap);
			
			parameterMap.put(GraphDACParams.queryStatementMap.name(), queryMap);
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public static String generateImportNodesCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Import Nodes' Query Generation Failed.]");

			List<Node> nodes = (List<Node>) parameterMap.get(GraphDACParams.nodes.name());
			if (null == nodes || nodes.size() <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE_LIST + " | [Import Nodes Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();

			Map<String, Object> queryMap = new HashMap<String, Object>();

			int index = 1;
			for (Node node : nodes) {
				String objPrefix = getString(index);
				StringBuilder templateQuery = new StringBuilder();
				Map<String, Object> templateQueryMap = new HashMap<String, Object>();
				Map<String, Object> templateParamValueMap = new HashMap<String, Object>();
				// Sample: CREATE (ee:Person { name: "Emil", from: "Sweden",
				// klout:99 })
				query.append(GraphDACParams.MERGE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS)
						.append(objPrefix + CypherQueryConfigurationConstants.COLON).append(graphId)
						.append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
						.append(getMatchCriteriaString(graphId, node))
						.append(getPropertyObjectAttributeString(node)).append(CypherQueryConfigurationConstants.COMMA)
						.append(getSystemPropertyString(node, date)).append(CypherQueryConfigurationConstants.COMMA)
						.append(getAuditPropertyString(node, date, false))
						.append(CypherQueryConfigurationConstants.COMMA)
						.append(getVersionKeyPropertyString(node, date, false))
						.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
						.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

				// Adding 'ON CREATE SET n.created=timestamp()' Clause
				query.append(getOnCreateSetString(objPrefix, date, node))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

				// Adding 'ON MATCH SET' Clause
				query.append(getOnMatchSetString(objPrefix, date, node))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

				// Return Node
				query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
						.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(objPrefix);

				// Template Query
				Map<String, Object> mpMap = getMetadataCypherQueryMap(node);
				Map<String, Object> ocsMap = getOnCreateSetQueryMap(objPrefix, date, node);
				Map<String, Object> omsMap = getOnMatchSetQueryMap(objPrefix, date, node, false);
				templateQuery.append(GraphDACParams.MERGE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS)
						.append(objPrefix + CypherQueryConfigurationConstants.COLON).append(graphId)
						.append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
						.append(getMatchCriteriaString(graphId, node))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
						.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
						.append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(ocsMap.get(GraphDACParams.query.name()))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(omsMap.get(GraphDACParams.query.name()));

				// Return Node
				templateQuery.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
						.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(objPrefix);
				templateParamValueMap.putAll((Map<String, Object>) mpMap.get(GraphDACParams.paramValueMap.name()));
				templateParamValueMap.putAll((Map<String, Object>) ocsMap.get(GraphDACParams.paramValueMap.name()));
				templateParamValueMap.putAll((Map<String, Object>) omsMap.get(GraphDACParams.paramValueMap.name()));

				templateQueryMap.put(GraphDACParams.query.name(), templateQuery.toString());
				templateQueryMap.put(GraphDACParams.paramValueMap.name(), templateParamValueMap);

				queryMap.put(node.getIdentifier(), templateQueryMap);
			}
			parameterMap.put(GraphDACParams.queryStatementMap.name(), queryMap);
		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateUpsertRootNodeCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			Node rootNode = (Node) parameterMap.get(GraphDACParams.rootNode.name());
			if (null == rootNode)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_ROOT_NODE + " | [Create Root Node Query Generation Failed.]");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			String date = DateUtils.formatCurrentDate();

			query.append(GraphDACParams.MERGE.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(rootNode.getGraphId()).append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getMatchCriteriaString(graphId, rootNode))
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS);

			// Adding 'ON CREATE SET n.created=timestamp()' Clause
			query.append(
					getOnCreateSetString(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, date, rootNode));

			// Return Node
			query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT);
		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateUpdatePropertyValueCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | [Update Property Value Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | [Update Property Value Query Generation Failed.]");

			Property property = (Property) parameterMap.get(GraphDACParams.property.name());
			if (null == property)
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY
								+ " | [Update Property Value Query Generation Failed.]");

			query.append(GraphDACParams.MATCH.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.WHERE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(nodeId)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.SET.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.DOT).append(property.getPropertyName())
					.append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(property.getPropertyValue())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);

		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateUpdatePropertyValuesCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			TelemetryManager.log("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | [Update Property Values Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | [Update Property Values Query Generation Failed.]");

			Map<String, Object> metadata = (Map<String, Object>) parameterMap.get(GraphDACParams.metadata.name());
			if (null == metadata)
				throw new ClientException(DACErrorCodeConstants.INVALID_METADATA.name(),
						DACErrorMessageConstants.INVALID_PROPERTIES
								+ " | [Update Property Values Query Generation Failed.]");

			query.append(GraphDACParams.MATCH.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.WHERE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(nodeId)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.SET.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(getMetadataStringForCypherQuery(
							CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, metadata));

		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateRemovePropertyValueCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | [Remove Property Value Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | [Remove Property Value Query Generation Failed.]");

			String key = (String) parameterMap.get(GraphDACParams.key.name());
			if (null == key)
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY_KEY
								+ " | [Remove Property Value Query Generation Failed.]");

			query.append(GraphDACParams.MATCH.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.WHERE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(nodeId)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.SET.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.DOT).append(key)
					.append(CypherQueryConfigurationConstants.EQUALS).append("null");

		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateRemovePropertyValuesCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | [Remove Property Values Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | [Remove Property Values Query Generation Failed.]");

			List<String> keys = (List<String>) parameterMap.get(GraphDACParams.keys.name());
			if (null == keys)
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY_KEY_LIST
								+ " | [Remove Property Values Query Generation Failed.]");

			query.append(GraphDACParams.MATCH.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.WHERE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(nodeId)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.SET.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(getRemoveKeysStringForCypherQuery(
							CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, keys));

		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateDeleteNodeCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | [Remove Property Values Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | [Remove Property Values Query Generation Failed.]");

			query.append("MATCH (a:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + nodeId
					+ "'}) DETACH DELETE a");
		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unused")
	private static String getClassicNodeDeleteCypherQuery(String graphId, String nodeId) {
		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(nodeId)) {
			query.append(GraphDACParams.MATCH.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.DASH)
					.append(CypherQueryConfigurationConstants.OPEN_SQUARE_BRACKETS)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_RELATION_OBJECT)
					.append(CypherQueryConfigurationConstants.CLOSE_SQUARE_BRACKETS)
					.append(CypherQueryConfigurationConstants.DASH)
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.WHERE.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(nodeId)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.DELETE.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.COMMA)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_RELATION_OBJECT);
		}
		return query.toString();
	}
}
