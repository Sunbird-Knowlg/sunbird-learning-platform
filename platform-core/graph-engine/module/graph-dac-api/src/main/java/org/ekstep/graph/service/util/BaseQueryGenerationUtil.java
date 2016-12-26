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
import org.ekstep.graph.service.common.CypherQueryConfigurationConstants;

import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;

public class BaseQueryGenerationUtil {

	protected static Logger LOGGER = LogManager.getLogger(BaseQueryGenerationUtil.class.getName());

	protected static String getPropertyObject(Node node, String date, boolean isUpdateOperation) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Sample: { name: "Emil", from: "Sweden", klout:99 }
			query.append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS);
			if (null != node.getMetadata() & !node.getMetadata().isEmpty())
				for (Entry<String, Object> entry : node.getMetadata().entrySet())
					query.append(entry.getKey()).append(CypherQueryConfigurationConstants.COLON)
							.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(entry.getValue())
							.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
							.append(CypherQueryConfigurationConstants.COMMA);
			StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA);
			query.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS);
		}
		return query.toString();
	}

	protected static String getPropertyObjectAttributeString(Node node) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && null != node.getMetadata() && !node.getMetadata().isEmpty()) {
			// Sample: name: "Emil", from: "Sweden", klout:99
			for (Entry<String, Object> entry : node.getMetadata().entrySet())
				query.append(entry.getKey()).append(CypherQueryConfigurationConstants.COLON)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(entry.getValue())
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);
		}
		StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA);

		LOGGER.info("Returning Property Object Attribute String: " + query.toString());
		return query.toString();
	}

	protected static String getSystemPropertyString(Node node, String date) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), DateUtils.parse(date).getTime()));

			// Adding 'IL_UNIQUE_ID' Property
			query.append(SystemProperties.IL_UNIQUE_ID.name()).append(CypherQueryConfigurationConstants.COLON)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getIdentifier())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(CypherQueryConfigurationConstants.COLON)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getNodeType())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType()))
				query.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name())
						.append(CypherQueryConfigurationConstants.COLON)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getObjectType())
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);
		}

		LOGGER.info("Returning System Property String: " + query.toString());
		return query.toString();
	}

	protected static String getAuditPropertyString(Node node, String date, boolean isUpdateOnly) {
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Adding 'createdOn' Property
			if (BooleanUtils.isFalse(isUpdateOnly))
				query.append(AuditProperties.createdOn.name()).append(CypherQueryConfigurationConstants.COLON)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'lastUpdatedOn' Property
			query.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.COLON)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);
		}

		LOGGER.info("Returning Audit Property String: " + query.toString());
		return query.toString();
	}

	protected static String getVersionKeyPropertyString(Node node, String date, boolean isUpdateOnly) {
		LOGGER.debug("Graph Engine Node: ", node);
		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Adding 'versionKey' Property
			query.append(GraphDACParams.versionKey.name()).append(CypherQueryConfigurationConstants.COLON)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(Long.toString(DateUtils.parse(date).getTime()))
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);
		}

		LOGGER.info("Returning 'versionKey' Property String: " + query.toString());
		return query.toString();
	}

	protected static String getOnCreateSetString(String objectVariableName, String date, Node node) {
		LOGGER.debug("Cypher Query Node Object Variable Name: ", objectVariableName);
		LOGGER.debug("Date: ", date);
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(objectVariableName) && StringUtils.isNotBlank(date)) {
			// Adding Clause 'ON MATCH SET'
			query.append(GraphDACParams.ON.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.MATCH.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.SET.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding 'IL_UNIQUE_ID' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(SystemProperties.IL_UNIQUE_ID.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getIdentifier())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getNodeType())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType()))
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name())
						.append(CypherQueryConfigurationConstants.EQUALS)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getObjectType())
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);

			// Adding Property String
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(AuditProperties.createdOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getIdentifier())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'lastUpdatedOn' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

		}

		LOGGER.info("Returning 'ON_CREATE_SET' Query Part String: " + query.toString());
		return query.toString();
	}

	protected static String getOnMatchSetString(String objectVariableName, String date, Node node) {
		LOGGER.debug("Cypher Query Node Object Variable Name: ", objectVariableName);
		LOGGER.debug("Date: ", date);
		LOGGER.debug("Graph Engine Node: ", node);

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(objectVariableName) && StringUtils.isNotBlank(date)) {
			// Adding Clause 'ON MATCH SET'
			query.append(GraphDACParams.ON.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.MATCH.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.SET.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding 'IL_UNIQUE_ID' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(SystemProperties.IL_UNIQUE_ID.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getIdentifier())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getNodeType())
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType()))
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name())
						.append(CypherQueryConfigurationConstants.EQUALS)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(node.getObjectType())
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);

			// Adding 'lastUpdatedOn' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

		}

		LOGGER.info("Returning 'ON_MATCH_SET' Query Part String: " + query.toString());
		return query.toString();
	}

	protected static String getMetadataStringForCypherQuery(String objectVariableName, Map<String, Object> metadata) {
		StringBuilder queryPart = new StringBuilder();
		if (StringUtils.isNotBlank(objectVariableName) && null != metadata && !metadata.isEmpty()) {
			for (Entry<String, Object> entry : metadata.entrySet())
				queryPart.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(entry.getKey()).append(CypherQueryConfigurationConstants.EQUALS)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(entry.getValue())
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);
			StringUtils.removeEnd(queryPart.toString(), CypherQueryConfigurationConstants.COMMA);
		}
		return queryPart.toString();
	}

	protected static String getRemoveKeysStringForCypherQuery(String objectVariableName, List<String> keys) {
		StringBuilder queryPart = new StringBuilder();
		if (StringUtils.isNotBlank(objectVariableName) && null != keys && keys.size() > 0) {
			for (String key : keys)
				queryPart.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT).append(key)
						.append(CypherQueryConfigurationConstants.EQUALS)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append("null")
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);
			StringUtils.removeEnd(queryPart.toString(), CypherQueryConfigurationConstants.COMMA);
		}
		return queryPart.toString();
	}

	protected static String getString(int n) {
		char[] buf = new char[(int) floor(log(25 * (n + 1)) / log(26))];
		for (int i = buf.length - 1; i >= 0; i--) {
			n--;
			buf[i] = (char) ('A' + n % 26);
			n /= 26;
		}
		return new String(buf);
	}
}
