package org.ekstep.graph.service.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.CypherQueryConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.common.dto.Property;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;

public class NodeQueryGenerationUtil extends BaseQueryGenerationUtil {

	private static Logger LOGGER = LogManager.getLogger(NodeQueryGenerationUtil.class.getName());

	public static String generateCreateNodeCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			Node node = (Node) parameterMap.get(GraphDACParams.node.name());
			if (null == node)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE + " | [Create Node Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);

			// Sample: CREATE (ee:Person { name: "Emil", from: "Sweden",
			// klout:99 })
			query.append(GraphDACParams.CREATE.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(node.getGraphId()).append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(node)).append(CypherQueryConfigurationConstants.COMMA)
					.append(getSystemPropertyString(node, date)).append(CypherQueryConfigurationConstants.COMMA)
					.append(getAuditPropertyString(node, date, false)).append(CypherQueryConfigurationConstants.COMMA)
					.append(getVersionKeyPropertyString(node, date, false))
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Return Node
			query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT);
		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateUpsertNodeCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			Node node = (Node) parameterMap.get(GraphDACParams.node.name());
			if (null == node)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE + " | [Create Node Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);

			// Sample:
			// MERGE (n:Employee {identifier: "4", name: "Ilimi", address:
			// "Indore"})
			// ON CREATE SET n.created=timestamp()
			// ON MATCH SET
			// n.counter= coalesce(n.counter, 0) + 1,
			// n.accessTime = timestamp()
			query.append(GraphDACParams.MERGE.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(node.getGraphId()).append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(node))
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding 'ON CREATE SET n.created=timestamp()' Clause
			query.append(getOnCreateSetString(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, date, node))
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding 'ON MATCH SET' Clause
			query.append(getOnMatchSetString(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, date, node))
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Return Node
			query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateUpdateNodeCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			Node node = (Node) parameterMap.get(GraphDACParams.node.name());
			if (null == node)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE + " | [Create Node Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);

			// Sample
			// MERGE (n:Employee {identifier: "5"})
			// SET n.address = "Bangalore", n.name = "MD", n.created = null
			query.append(GraphDACParams.MERGE.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(node.getGraphId()).append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(node)).append(CypherQueryConfigurationConstants.COMMA)
					.append(getAuditPropertyString(node, date, true)).append(CypherQueryConfigurationConstants.COMMA)
					.append(getVersionKeyPropertyString(node, date, true))
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Return Node
			query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT);

		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateImportNodesCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			List<Node> nodes = (List<Node>) parameterMap.get(GraphDACParams.nodes.name());
			if (null == nodes || nodes.size() <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE_LIST + " | [Import Nodes Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);

			Map<String, Object> queryMap = new HashMap<String, Object>();

			int index = 1;
			for (Node node : nodes) {
				String objPrefix = getString(index);
				StringBuilder templateQuery = new StringBuilder();
				Map<String, Object> templateQueryMap = new HashMap<String, Object>();
				Map<String, Object> templateParamValueMap = new HashMap<String, Object>();
				// Sample: CREATE (ee:Person { name: "Emil", from: "Sweden",
				// klout:99 })
				query.append(GraphDACParams.CREATE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS)
						.append(objPrefix + CypherQueryConfigurationConstants.COLON).append(node.getGraphId())
						.append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
						.append(getPropertyObjectAttributeString(node)).append(CypherQueryConfigurationConstants.COMMA)
						.append(getSystemPropertyString(node, date)).append(CypherQueryConfigurationConstants.COMMA)
						.append(getAuditPropertyString(node, date, false))
						.append(CypherQueryConfigurationConstants.COMMA)
						.append(getVersionKeyPropertyString(node, date, false))
						.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
						.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

				// Return Node
				query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
						.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(objPrefix);

				// Template Query
				Map<String, Object> mpMap = getMetadataCypherQueryMap(node);
				Map<String, Object> spMap = getSystemPropertyQueryMap(node, date);
				Map<String, Object> apMap = getAuditPropertyQueryMap(node, date, false);
				Map<String, Object> vpMap = getVersionKeyPropertyQueryMap(node, date, false);
				templateQuery.append(GraphDACParams.CREATE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS)
						.append(objPrefix + CypherQueryConfigurationConstants.COLON).append(node.getGraphId())
						.append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
						.append(mpMap.get(GraphDACParams.query.name())).append(CypherQueryConfigurationConstants.COMMA)
						.append(spMap.get(GraphDACParams.query.name())).append(CypherQueryConfigurationConstants.COMMA)
						.append(apMap.get(GraphDACParams.query.name())).append(CypherQueryConfigurationConstants.COMMA)
						.append(vpMap.get(GraphDACParams.query.name()))
						.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
						.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
						.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
						.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(objPrefix);
				templateParamValueMap.putAll((Map<String, Object>) mpMap.get(GraphDACParams.paramValueMap.name()));
				templateParamValueMap.putAll((Map<String, Object>) spMap.get(GraphDACParams.paramValueMap.name()));
				templateParamValueMap.putAll((Map<String, Object>) apMap.get(GraphDACParams.paramValueMap.name()));
				templateParamValueMap.putAll((Map<String, Object>) vpMap.get(GraphDACParams.paramValueMap.name()));

				templateQueryMap.put(GraphDACParams.query.name(), templateQuery.toString());
				templateQueryMap.put(GraphDACParams.paramValueMap.name(), templateParamValueMap);

				queryMap.put(node.getIdentifier(), templateQueryMap);
			}
			parameterMap.put(GraphDACParams.queryStatementMap.name(), queryMap);
		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateUpsertRootNodeCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			Node rootNode = (Node) parameterMap.get(GraphDACParams.rootNode.name());
			if (null == rootNode)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_ROOT_NODE + " | [Create Root Node Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);

			query.append(GraphDACParams.MERGE.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(rootNode.getGraphId()).append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(rootNode))
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS);

			// Adding 'ON CREATE SET n.created=timestamp()' Clause
			query.append(
					getOnCreateSetString(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, date, rootNode));

			// Adding 'ON MATCH SET' Clause
			query.append(
					getOnMatchSetString(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, date, rootNode));

			// Return Node
			query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT);
		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateUpdatePropertyValueCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
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

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);
			// Sample:
			// MATCH (n:Employee)
			// WHERE n.name = "Azhar"
			// SET n.owns = "BMW"
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

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateUpdatePropertyValuesCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
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

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);
			// Sample:
			// MATCH (n:Employee)
			// WHERE n.name = "Azhar"
			// SET n.owns = "BMW", n.address = "Indore"
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

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateRemovePropertyValueCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
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

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);
			// Sample:
			// MATCH (n:Employee)
			// WHERE n.name = "Azhar"
			// SET n.owns = "BMW"
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
					.append(CypherQueryConfigurationConstants.DOT).append(key)
					.append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append("null")
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);

		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateRemovePropertyValuesCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
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

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);
			// Sample:
			// MATCH (n:Employee)
			// WHERE n.name = "Azhar"
			// SET n.owns = "BMW"
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
					.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(getRemoveKeysStringForCypherQuery(
							CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, keys));

		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateDeleteNodeCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
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

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unused")
	private static String getClassicNodeDeleteCypherQuery(String graphId, String nodeId) {
		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(nodeId)) {
			// Sample:
			// MATCH (n)-[r]-()
			// WHERE id(n) = 5
			// DELETE r, n
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
