package org.ekstep.graph.service.util;

import static java.lang.Math.floor;
import static java.lang.Math.log;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;

public class QueryGenerationUtil {
	
	private static Logger LOGGER = LogManager.getLogger(QueryGenerationUtil.class.getName());
	
	private final static String OPEN_CURLY_BRACKETS = "{";

	private final static String CLOSE_CURLY_BRACKETS = "}";

	private final static String OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE = "(ee:";

	private final static String CLOSE_COMMON_BRACKETS = ")";

	private final static String COLON = ": ";

	private final static String SINGLE_QUOTE = "'";

	private final static String COMMA = ", ";

	private final static String BLANK_SPACE = " ";

	private final static String EQUALS = "=";

	private final static String DEFAULT_CYPHER_NODE_OBJECT = "ee";
	
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
					.append(CLOSE_COMMON_BRACKETS);

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
					.append(CLOSE_COMMON_BRACKETS);

			// Adding 'ON CREATE SET n.created=timestamp()' Clause
			query.append(getOnCreateSetString(DEFAULT_CYPHER_NODE_OBJECT, date, node));

			// Adding 'ON MATCH SET' Clause
			query.append(getOnMatchSetString(DEFAULT_CYPHER_NODE_OBJECT, date, node));

			// Return Node
			query.append(BLANK_SPACE).append(GraphDACParams.RETURN.name()).append(BLANK_SPACE)
					.append(DEFAULT_CYPHER_NODE_OBJECT);

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
					.append(CLOSE_COMMON_BRACKETS);

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
			if (null == nodes)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_NODE_LIST + " | [Import Nodes Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();
			LOGGER.info("Date: " + date);

			if (null != nodes && nodes.size() > 0) {
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
		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateSearchNodeCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {

		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateCreateUniqueConstraintCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {

		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateCreateIndexCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {

		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unused")
	private static String getPropertyObject(Node node, String date, boolean isUpdateOperation) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Sample: { name: "Emil", from: "Sweden", klout:99 }
			query.append(OPEN_CURLY_BRACKETS);
			if (null != node.getMetadata() & !node.getMetadata().isEmpty())
				for (Entry<String, Object> entry : node.getMetadata().entrySet())
					query.append(entry.getKey()).append(COLON).append(SINGLE_QUOTE).append(entry.getValue())
							.append(SINGLE_QUOTE).append(COMMA);
			StringUtils.removeEnd(query.toString(), COMMA);
			query.append(CLOSE_CURLY_BRACKETS);
		}
		return query.toString();
	}

	private static String getPropertyObjectAttributeString(Node node) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && null != node.getMetadata() && !node.getMetadata().isEmpty()) {
			// Sample: name: "Emil", from: "Sweden", klout:99
			for (Entry<String, Object> entry : node.getMetadata().entrySet())
				query.append(entry.getKey()).append(COLON).append(SINGLE_QUOTE).append(entry.getValue())
						.append(SINGLE_QUOTE).append(COMMA);
		}
		StringUtils.removeEnd(query.toString(), COMMA);

		LOGGER.info("Returning Property Object Attribute String: " + query.toString());
		return query.toString();
	}

	private static String getSystemPropertyString(Node node, String date) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), DateUtils.parse(date).getTime()));

			// Adding 'IL_UNIQUE_ID' Property
			query.append(SystemProperties.IL_UNIQUE_ID.name()).append(COLON).append(SINGLE_QUOTE)
					.append(node.getIdentifier()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(COLON).append(SINGLE_QUOTE)
					.append(node.getNodeType()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType()))
				query.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).append(COLON).append(SINGLE_QUOTE)
						.append(node.getObjectType()).append(SINGLE_QUOTE);
		}

		LOGGER.info("Returning System Property String: " + query.toString());
		return query.toString();
	}

	private static String getAuditPropertyString(Node node, String date, boolean isUpdateOnly) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Adding 'createdOn' Property
			if (BooleanUtils.isFalse(isUpdateOnly))
				query.append(AuditProperties.createdOn.name()).append(COLON).append(SINGLE_QUOTE).append(date)
						.append(SINGLE_QUOTE).append(COMMA);

			// Adding 'lastUpdatedOn' Property
			query.append(AuditProperties.lastUpdatedOn.name()).append(COLON).append(SINGLE_QUOTE).append(date)
					.append(SINGLE_QUOTE);
		}

		LOGGER.info("Returning Audit Property String: " + query.toString());
		return query.toString();
	}

	private static String getVersionKeyPropertyString(Node node, String date, boolean isUpdateOnly) {
		LOGGER.debug("Graph Engine Node: ", node);
		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Adding 'versionKey' Property
			query.append(GraphDACParams.versionKey.name()).append(COLON).append(SINGLE_QUOTE)
					.append(Long.toString(DateUtils.parse(date).getTime())).append(SINGLE_QUOTE);
		}

		LOGGER.info("Returning 'versionKey' Property String: " + query.toString());
		return query.toString();
	}

	private static String getOnCreateSetString(String objectVariableName, String date, Node node) {
		LOGGER.debug("Cypher Query Node Object Variable Name: ", objectVariableName);
		LOGGER.debug("Date: ", date);
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(objectVariableName) && StringUtils.isNotBlank(date)) {
			// Adding Clause 'ON MATCH SET'
			query.append(GraphDACParams.ON.name()).append(BLANK_SPACE).append(GraphDACParams.MATCH.name())
					.append(BLANK_SPACE).append(GraphDACParams.SET.name()).append(BLANK_SPACE);

			// Adding 'IL_UNIQUE_ID' Property
			query.append(SystemProperties.IL_UNIQUE_ID.name()).append(EQUALS).append(SINGLE_QUOTE)
					.append(node.getIdentifier()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(EQUALS).append(SINGLE_QUOTE)
					.append(node.getNodeType()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType()))
				query.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).append(EQUALS).append(SINGLE_QUOTE)
						.append(node.getObjectType()).append(SINGLE_QUOTE);

			// Adding Property String
			query.append(AuditProperties.createdOn.name()).append(EQUALS).append(SINGLE_QUOTE)
					.append(node.getIdentifier()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'lastUpdatedOn' Property
			query.append(AuditProperties.lastUpdatedOn.name()).append(EQUALS).append(SINGLE_QUOTE).append(date)
					.append(SINGLE_QUOTE);

		}

		LOGGER.info("Returning 'ON_CREATE_SET' Query Part String: " + query.toString());
		return query.toString();
	}

	private static String getOnMatchSetString(String objectVariableName, String date, Node node) {
		LOGGER.debug("Cypher Query Node Object Variable Name: ", objectVariableName);
		LOGGER.debug("Date: ", date);
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(objectVariableName) && StringUtils.isNotBlank(date)) {
			// Adding Clause 'ON MATCH SET'
			query.append(GraphDACParams.ON.name()).append(BLANK_SPACE).append(GraphDACParams.MATCH.name())
					.append(BLANK_SPACE).append(GraphDACParams.SET.name()).append(BLANK_SPACE);

			// Adding 'IL_UNIQUE_ID' Property
			query.append(SystemProperties.IL_UNIQUE_ID.name()).append(EQUALS).append(SINGLE_QUOTE)
					.append(node.getIdentifier()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(EQUALS).append(SINGLE_QUOTE)
					.append(node.getNodeType()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType()))
				query.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).append(EQUALS).append(SINGLE_QUOTE)
						.append(node.getObjectType()).append(SINGLE_QUOTE).append(COMMA);

			// Adding 'lastUpdatedOn' Property
			query.append(AuditProperties.lastUpdatedOn.name()).append(EQUALS).append(SINGLE_QUOTE).append(date)
					.append(SINGLE_QUOTE);

		}

		LOGGER.info("Returning 'ON_MATCH_SET' Query Part String: " + query.toString());
		return query.toString();
	}

	private static String getString(int n) {
		char[] buf = new char[(int) floor(log(25 * (n + 1)) / log(26))];
		for (int i = buf.length - 1; i >= 0; i--) {
			n--;
			buf[i] = (char) ('A' + n % 26);
			n /= 26;
		}
		return new String(buf);
	}
}
