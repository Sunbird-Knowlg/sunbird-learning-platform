package org.ekstep.graph.service.util;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
			query.append(GraphDACParams.CREATE.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(node.getGraphId()).append(OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(node)).append(COMMA)
					.append(getSystemPropertyString(node, date)).append(COMMA)
					.append(getAuditPropertyString(node, date, false)).append(COMMA)
					.append(getVersionKeyPropertyString(node, date, false)).append(CLOSE_CURLY_BRACKETS)
					.append(CLOSE_COMMON_BRACKETS).append(BLANK_SPACE);

			// Return Node
			query.append(BLANK_SPACE).append(GraphDACParams.RETURN.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT);
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
			query.append(GraphDACParams.MERGE.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(node.getGraphId()).append(OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(node)).append(CLOSE_CURLY_BRACKETS)
					.append(CLOSE_COMMON_BRACKETS).append(BLANK_SPACE);

			// Adding 'ON CREATE SET n.created=timestamp()' Clause
			query.append(getOnCreateSetString(DEFAULT_CYPHER_NODE_OBJECT, date, node)).append(BLANK_SPACE);

			// Adding 'ON MATCH SET' Clause
			query.append(getOnMatchSetString(DEFAULT_CYPHER_NODE_OBJECT, date, node)).append(BLANK_SPACE);

			// Return Node
			query.append(BLANK_SPACE).append(GraphDACParams.RETURN.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT).append(BLANK_SPACE);

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
			query.append(GraphDACParams.MERGE.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(node.getGraphId()).append(OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(node)).append(COMMA)
					.append(getAuditPropertyString(node, date, true)).append(COMMA)
					.append(getVersionKeyPropertyString(node, date, true)).append(CLOSE_CURLY_BRACKETS)
					.append(CLOSE_COMMON_BRACKETS).append(BLANK_SPACE);

			// Return Node
			query.append(BLANK_SPACE).append(GraphDACParams.RETURN.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT);

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
			if (null == nodes || nodes.size() > 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE_LIST + " | [Import Nodes Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);

			int index = 0;
			for (Node node : nodes) {
				String objPrefix = getString(index++);
				// Sample: CREATE (ee:Person { name: "Emil", from: "Sweden",
				// klout:99 })
				query.append(GraphDACParams.CREATE.name()).append(objPrefix).append(node.getGraphId())
						.append(OPEN_CURLY_BRACKETS).append(getPropertyObjectAttributeString(node)).append(COMMA)
						.append(getSystemPropertyString(node, date)).append(COMMA)
						.append(getAuditPropertyString(node, date, false)).append(COMMA)
						.append(getVersionKeyPropertyString(node, date, false)).append(CLOSE_CURLY_BRACKETS)
						.append(CLOSE_COMMON_BRACKETS).append(BLANK_SPACE);

				// Return Node
				query.append(BLANK_SPACE).append(GraphDACParams.RETURN.name()).append(BLANK_SPACE)
						.append(DEFAULT_CYPHER_NODE_OBJECT);
			}
		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateUpsertRoodNodeCypherQuery(Map<String, Object> parameterMap) {
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

			query.append(GraphDACParams.MERGE.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(rootNode.getGraphId()).append(OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(rootNode)).append(CLOSE_CURLY_BRACKETS)
					.append(CLOSE_COMMON_BRACKETS);

			// Adding 'ON CREATE SET n.created=timestamp()' Clause
			query.append(getOnCreateSetString(DEFAULT_CYPHER_NODE_OBJECT, date, rootNode));

			// Adding 'ON MATCH SET' Clause
			query.append(getOnMatchSetString(DEFAULT_CYPHER_NODE_OBJECT, date, rootNode));

			// Return Node
			query.append(BLANK_SPACE).append(GraphDACParams.RETURN.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT);
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
			query.append(GraphDACParams.MATCH.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(BLANK_SPACE).append(GraphDACParams.WHERE.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT).append(DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(EQUALS).append(SINGLE_QUOTE).append(nodeId).append(SINGLE_QUOTE).append(BLANK_SPACE)
					.append(GraphDACParams.SET.name()).append(BLANK_SPACE).append(DEFAULT_CYPHER_NODE_OBJECT)
					.append(DOT).append(property.getPropertyName()).append(EQUALS).append(SINGLE_QUOTE)
					.append(property.getPropertyValue()).append(SINGLE_QUOTE);

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
			query.append(GraphDACParams.MATCH.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(BLANK_SPACE).append(GraphDACParams.WHERE.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT).append(DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(EQUALS).append(SINGLE_QUOTE).append(nodeId).append(SINGLE_QUOTE).append(BLANK_SPACE)
					.append(GraphDACParams.SET.name()).append(BLANK_SPACE)
					.append(getMetadataStringForCypherQuery(DEFAULT_CYPHER_NODE_OBJECT, metadata));

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
			query.append(GraphDACParams.MATCH.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(BLANK_SPACE).append(GraphDACParams.WHERE.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT).append(DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(EQUALS).append(SINGLE_QUOTE).append(nodeId).append(SINGLE_QUOTE).append(BLANK_SPACE)
					.append(GraphDACParams.SET.name()).append(BLANK_SPACE).append(DEFAULT_CYPHER_NODE_OBJECT)
					.append(DOT).append(key).append(EQUALS).append(SINGLE_QUOTE).append("null").append(SINGLE_QUOTE);

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
			query.append(GraphDACParams.MATCH.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(BLANK_SPACE).append(GraphDACParams.WHERE.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT).append(DOT).append(SystemProperties.IL_UNIQUE_ID.name())
					.append(EQUALS).append(SINGLE_QUOTE).append(nodeId).append(SINGLE_QUOTE).append(BLANK_SPACE)
					.append(GraphDACParams.SET.name()).append(BLANK_SPACE)
					.append(getRemoveKeysStringForCypherQuery(DEFAULT_CYPHER_NODE_OBJECT, keys));

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
			query.append(GraphDACParams.MATCH.name()).append(OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CLOSE_COMMON_BRACKETS).append(DASH).append(OPEN_SQUARE_BRACKETS)
					.append(DEFAULT_CYPHER_RELATION_OBJECT).append(CLOSE_SQUARE_BRACKETS).append(DASH)
					.append(OPEN_COMMON_BRACKETS).append(CLOSE_COMMON_BRACKETS).append(BLANK_SPACE)
					.append(GraphDACParams.WHERE.name()).append(BLANK_SPACE).append(DEFAULT_CYPHER_NODE_OBJECT)
					.append(DOT).append(SystemProperties.IL_UNIQUE_ID.name()).append(EQUALS).append(SINGLE_QUOTE)
					.append(nodeId).append(SINGLE_QUOTE).append(BLANK_SPACE).append(GraphDACParams.DELETE.name())
					.append(BLANK_SPACE).append(DEFAULT_CYPHER_NODE_OBJECT).append(COMMA).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_RELATION_OBJECT);
		}
		return query.toString();
	}
}
