package org.sunbird.graph.service.util;

import static java.lang.Math.floor;
import static java.lang.Math.log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.AuditProperties;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.common.CypherQueryConfigurationConstants;
import org.sunbird.telemetry.logger.TelemetryManager;

public class BaseQueryGenerationUtil {

	protected static String getPropertyObject(Node node, String date, boolean isUpdateOperation) {

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
			query.append(StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
			query.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS);
		}
		return query.toString();
	}

	protected static String getPropertyObjectAttributeString(Node node) {

		StringBuilder query = new StringBuilder();
		if (null != node && null != node.getMetadata() && !node.getMetadata().isEmpty()) {
			// Sample: name: "Emil", from: "Sweden", klout:99
			for (Entry<String, Object> entry : node.getMetadata().entrySet())
				query.append(entry.getKey()).append(CypherQueryConfigurationConstants.COLON)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(getStringValue(entry.getValue()))
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);
		}
		query.append(StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));

		TelemetryManager.log("Returning Property Object Attribute String: " + query.toString());
		return query.toString();
	}

	protected static String getMatchCriteriaString(String graphId, Node node) {

		String matchCriteria = "";
		if (StringUtils.isNotBlank(graphId) && null != node) {
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp()));
			if (StringUtils.isBlank(node.getGraphId()))
				node.setGraphId(graphId);

			matchCriteria = SystemProperties.IL_UNIQUE_ID.name() + CypherQueryConfigurationConstants.BLANK_SPACE
					+ CypherQueryConfigurationConstants.COLON + CypherQueryConfigurationConstants.BLANK_SPACE
					+ CypherQueryConfigurationConstants.SINGLE_QUOTE + node.getIdentifier()
					+ CypherQueryConfigurationConstants.SINGLE_QUOTE;
		}

		return matchCriteria;
	}

	protected static Map<String, Object> getMetadataCypherQueryMap(Node node) {

		Map<String, Object> queryMap = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder();
		if (null != node && null != node.getMetadata() && !node.getMetadata().isEmpty()) {
			// Sample: name: "Emil", from: "Sweden", klout:99
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();
			for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
				query.append(entry.getKey() + ": { MD_" + entry.getKey() + " }, ");

				TelemetryManager.log("Adding Entry: " + entry.getKey() + "Value: "+ entry.getValue());

				// Populating Param Map
				paramValuesMap.put("MD_" + entry.getKey(), entry.getValue());
				TelemetryManager.log("Populating ParamMap:", paramValuesMap);
			}
			queryMap.put(GraphDACParams.paramValueMap.name(), paramValuesMap);
			queryMap.put(GraphDACParams.query.name(),
					StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
		}

		TelemetryManager.log("Returning Property Object Attribute String: " + query.toString());
		return queryMap;
	}

	protected static Map<String, Object> getMetadataCypherQueryMap(String objectVariableName,
			Map<String, Object> metadata) {
		TelemetryManager.log("Graph Engine metadata: ", metadata);

		Map<String, Object> queryMap = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder();
		if (null != metadata && !metadata.isEmpty()) {
			// Sample: name: "Emil", from: "Sweden", klout:99
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();
			for (Entry<String, Object> entry : metadata.entrySet()) {
				query.append(objectVariableName + CypherQueryConfigurationConstants.DOT + entry.getKey() + " =  { MD_"
						+ entry.getKey() + " }, ");

				TelemetryManager.log("Adding Entry: " + entry.getKey() + "Value: "+ entry.getValue());

				// Populating Param Map
				paramValuesMap.put("MD_" + entry.getKey(), entry.getValue());
				TelemetryManager.log("Populating ParamMap:", paramValuesMap);
			}

			queryMap.put(GraphDACParams.paramValueMap.name(), paramValuesMap);
			queryMap.put(GraphDACParams.query.name(),
					StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
		}

		TelemetryManager.log("Returning Property Object Attribute String: " + query.toString());
		return queryMap;
	}
	
	protected static String getSystemPropertyString(Node node, String date) {

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), Identifier.getUniqueIdFromTimestamp()));

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
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);
		}

		TelemetryManager.log("Returning System Property String: " + query.toString());
		return StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA);
	}

	protected static Map<String, Object> getSystemPropertyQueryMap(Node node, String date) {

		Map<String, Object> queryMap = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), Identifier.getUniqueIdFromTimestamp()));

			// Adding 'IL_UNIQUE_ID' Property
			query.append(
					SystemProperties.IL_UNIQUE_ID.name() + ":  { SP_" + SystemProperties.IL_UNIQUE_ID.name() + " }, ");
			paramValuesMap.put("SP_" + SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(SystemProperties.IL_SYS_NODE_TYPE.name() + ":  { SP_"
					+ SystemProperties.IL_SYS_NODE_TYPE.name() + " }, ");
			paramValuesMap.put("SP_" + SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType())) {
				query.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name() + ":  { SP_"
						+ SystemProperties.IL_FUNC_OBJECT_TYPE.name() + " }, ");
				paramValuesMap.put("SP_" + SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
			}

			queryMap.put(GraphDACParams.query.name(), StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
			queryMap.put(GraphDACParams.paramValueMap.name(), paramValuesMap);
		}

		TelemetryManager.log("Returning System Property String: " + query.toString());
		return queryMap;
	}

	protected static String getAuditPropertyString(Node node, String date, boolean isUpdateOnly) {

		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Adding 'createdOn' Property
			if (BooleanUtils.isFalse(isUpdateOnly))
				query.append(AuditProperties.createdOn.name()).append(CypherQueryConfigurationConstants.COLON)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(node.getMetadata().containsKey(AuditProperties.createdOn.name())
								? node.getMetadata().get(AuditProperties.createdOn.name())
								: date)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.COMMA);

			if (null != node.getMetadata()
					&& null == node.getMetadata().get(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name()))
				// Adding 'lastUpdatedOn' Property
				query.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.COLON)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);
		}

		TelemetryManager.log("Returning Audit Property String: " + query.toString());
		return query.toString();
	}

	protected static Map<String, Object> getAuditPropertyQueryMap(Node node, String date, boolean isUpdateOnly) {

		Map<String, Object> queryMap = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();
			// Adding 'createdOn' Property
			if (BooleanUtils.isFalse(isUpdateOnly)) {
				query.append(AuditProperties.createdOn.name() + ":  { AP_" + AuditProperties.createdOn.name() + " }, ");
				query.append(AuditProperties.lastStatusChangedOn.name() + ":  { AP_" + AuditProperties.createdOn.name() + " }, ");
				if(node.getMetadata().containsKey(AuditProperties.createdOn.name()))
					paramValuesMap.put("AP_" + AuditProperties.createdOn.name(), node.getMetadata().get(AuditProperties.createdOn.name()));
				else paramValuesMap.put("AP_" + AuditProperties.createdOn.name(), date);
			}


			if (null != node.getMetadata()
					&& null == node.getMetadata().get(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name())) {
				// Adding 'lastUpdatedOn' Property
				query.append(AuditProperties.lastUpdatedOn.name() + ":  { AP_" + AuditProperties.lastUpdatedOn.name()
						+ " }, ");
				paramValuesMap.put("AP_" + AuditProperties.lastUpdatedOn.name(), date);
			}

			queryMap.put(GraphDACParams.query.name(), StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
			queryMap.put(GraphDACParams.paramValueMap.name(), paramValuesMap);
		}

		TelemetryManager.log("Returning Audit Property Query Map: ", queryMap);
		return queryMap;
	}

	protected static String getVersionKeyPropertyString(Node node, String date, boolean isUpdateOnly) {
		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			// Adding 'versionKey' Property
			query.append(GraphDACParams.versionKey.name()).append(CypherQueryConfigurationConstants.COLON)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(Long.toString(DateUtils.parse(date).getTime()))
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE);
		}

		TelemetryManager.log("Returning 'versionKey' Property String: " + query.toString());
		return query.toString();
	}

	protected static Map<String, Object> getVersionKeyPropertyQueryMap(Node node, String date, boolean isUpdateOnly) {
		Map<String, Object> queryMap = new HashMap<String, Object>();
		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(date)) {
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();
			// Adding 'versionKey' Property
			query.append(GraphDACParams.versionKey.name() + ":  { VP_" + GraphDACParams.versionKey.name() + " }");
			paramValuesMap.put("VP_" + GraphDACParams.versionKey.name(),
					Long.toString(DateUtils.parse(date).getTime()));

			queryMap.put(GraphDACParams.query.name(),
					StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
			queryMap.put(GraphDACParams.paramValueMap.name(), paramValuesMap);
		}

		TelemetryManager.log("Returning 'versionKey' Property Query Map: ", queryMap);
		return queryMap;
	}

	protected static String getOnCreateSetString(String objectVariableName, String date, Node node) {
		StringBuilder query = new StringBuilder();
		if (null != node && StringUtils.isNotBlank(objectVariableName) && StringUtils.isNotBlank(date)) {
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), Identifier.getUniqueIdFromTimestamp()));

			// Adding Clause 'ON MATCH SET'
			query.append(GraphDACParams.ON.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.CREATE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
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
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
					.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
					.append(CypherQueryConfigurationConstants.COMMA);

			if (null != node.getMetadata()
					&& null == node.getMetadata().get(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name()))
				// Adding 'lastUpdatedOn' Property
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

		}

		TelemetryManager.log("Returning 'ON_CREATE_SET' Query Part String: " + query.toString());
		return query.toString();
	}

	protected static Map<String, Object> getOnCreateSetQueryMap(String objectVariableName, String date, Node node) {

		Map<String, Object> queryMap = new HashMap<String, Object>();
		if (null != node && StringUtils.isNotBlank(objectVariableName) && StringUtils.isNotBlank(date)) {
			StringBuilder query = new StringBuilder();
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();
			if (StringUtils.isBlank(node.getIdentifier()))
				node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), Identifier.getUniqueIdFromTimestamp()));

			// Adding Clause 'ON CREATE SET'
			query.append(GraphDACParams.ON.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.CREATE.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(GraphDACParams.SET.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding Metadata
			for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
				query.append(objectVariableName + CypherQueryConfigurationConstants.DOT + entry.getKey() + " =  { MD_"
						+ entry.getKey() + " }, ");

				TelemetryManager.log("Adding Entry: " + entry.getKey() + "Value: "+ entry.getValue());

				// Populating Param Map
				paramValuesMap.put("MD_" + entry.getKey(), entry.getValue());
				TelemetryManager.log("Populating ParamMap:", paramValuesMap);
			}

			// Adding 'IL_UNIQUE_ID' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(SystemProperties.IL_UNIQUE_ID.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(" { SP_" + SystemProperties.IL_UNIQUE_ID.name() + " } ")
					.append(CypherQueryConfigurationConstants.COMMA);
			paramValuesMap.put("SP_" + SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());

			// Adding 'IL_SYS_NODE_TYPE' Property
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(" { SP_" + SystemProperties.IL_SYS_NODE_TYPE.name() + " } ")
					.append(CypherQueryConfigurationConstants.COMMA);
			paramValuesMap.put("SP_" + SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());

			// Adding 'IL_FUNC_OBJECT_TYPE' Property
			if (StringUtils.isNotBlank(node.getObjectType()))
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(SystemProperties.IL_FUNC_OBJECT_TYPE.name())
						.append(CypherQueryConfigurationConstants.EQUALS)
						.append(" { SP_" + SystemProperties.IL_FUNC_OBJECT_TYPE.name() + " } ")
						.append(CypherQueryConfigurationConstants.COMMA);
			paramValuesMap.put("SP_" + SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());

			// Adding Property String
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(AuditProperties.createdOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(" { AP_" + AuditProperties.createdOn.name() + " } ")
					.append(CypherQueryConfigurationConstants.COMMA);
			paramValuesMap.put("AP_" + AuditProperties.createdOn.name(), date);

			if (null != node.getMetadata()
					&& null == node.getMetadata().get(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name())) {
				// Adding 'lastUpdatedOn' Property
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
						.append(" { AP_" + AuditProperties.lastUpdatedOn.name() + " } ")
						.append(CypherQueryConfigurationConstants.COMMA);
				paramValuesMap.put("AP_" + AuditProperties.lastUpdatedOn.name(), date);
			}

			String versionKey = Long.toString(DateUtils.parse(date).getTime());
			query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
					.append(GraphDACParams.versionKey.name()).append(CypherQueryConfigurationConstants.EQUALS)
					.append(" { MD_" + GraphDACParams.versionKey.name() + " } ")
					.append(CypherQueryConfigurationConstants.COMMA);
			paramValuesMap.put("MD_" + GraphDACParams.versionKey.name(), versionKey);

			queryMap.put(GraphDACParams.query.name(),
					StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
			queryMap.put(GraphDACParams.paramValueMap.name(), paramValuesMap);
		}

		TelemetryManager.log("Returning 'ON_CREATE_SET' Query Map: ", queryMap);
		return queryMap;
	}

	protected static String getOnMatchSetString(String objectVariableName, String date, Node node) {

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

			if (null != node.getMetadata()
					&& null == node.getMetadata().get(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name()))
				// Adding 'lastUpdatedOn' Property
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE).append(date)
						.append(CypherQueryConfigurationConstants.SINGLE_QUOTE)
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

		}

		TelemetryManager.log("Returning 'ON_MATCH_SET' Query Part String: " + query.toString());
		return query.toString();
	}

	protected static Map<String, Object> getOnMatchSetQueryMap(String objectVariableName, String date, Node node,
			boolean merge) {

		Map<String, Object> queryMap = new HashMap<String, Object>();
		if (null != node && StringUtils.isNotBlank(objectVariableName) && StringUtils.isNotBlank(date)) {
			StringBuilder query = new StringBuilder();
			Map<String, Object> paramValuesMap = new HashMap<String, Object>();

			if (merge)
				// Adding Clause 'ON MATCH SET'
				query.append(GraphDACParams.ON.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(GraphDACParams.MATCH.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE)
						.append(GraphDACParams.SET.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE);
			else
				// Adding Clause 'SET'
				query.append(GraphDACParams.SET.name()).append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding Metadata
			for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
				if (!StringUtils.equalsIgnoreCase(entry.getKey(), GraphDACParams.versionKey.name())) {
					query.append(objectVariableName + CypherQueryConfigurationConstants.DOT + entry.getKey()
							+ " =  { MD_"
						+ entry.getKey() + " }, ");

				TelemetryManager.log("Adding Entry: " + entry.getKey() + "Value: "+ entry.getValue());

				// Populating Param Map
				paramValuesMap.put("MD_" + entry.getKey(), entry.getValue());
				TelemetryManager.log("Populating ParamMap:", paramValuesMap);
				}
			}

			if (null != node.getMetadata()
					&& null == node.getMetadata().get(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name())) {
				// Adding 'lastUpdatedOn' Property
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(AuditProperties.lastUpdatedOn.name()).append(CypherQueryConfigurationConstants.EQUALS)
						.append(" { AP_" + AuditProperties.lastUpdatedOn.name() + " } ")
						.append(CypherQueryConfigurationConstants.COMMA);
				paramValuesMap.put("AP_" + AuditProperties.lastUpdatedOn.name(), date);
			}

			if (StringUtils.isBlank((String) node.getMetadata().get(GraphDACParams.versionKey.name()))) {
				String versionKey = Long.toString(DateUtils.parse(date).getTime());
				query.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT)
						.append(GraphDACParams.versionKey.name()).append(CypherQueryConfigurationConstants.EQUALS)
						.append(" { MD_" + GraphDACParams.versionKey.name() + " } ")
						.append(CypherQueryConfigurationConstants.COMMA);
				paramValuesMap.put("MD_" + GraphDACParams.versionKey.name(), versionKey);
			}

			queryMap.put(GraphDACParams.query.name(),
					StringUtils.removeEnd(query.toString(), CypherQueryConfigurationConstants.COMMA));
			queryMap.put(GraphDACParams.paramValueMap.name(), paramValuesMap);
		}
		TelemetryManager.log("Returning 'ON_MATCH_SET' Query Map: ", queryMap);
		return queryMap;
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
		}
		return StringUtils.removeEnd(queryPart.toString(), CypherQueryConfigurationConstants.COMMA);
	}

	protected static String getRemoveKeysStringForCypherQuery(String objectVariableName, List<String> keys) {
		StringBuilder queryPart = new StringBuilder();
		if (StringUtils.isNotBlank(objectVariableName) && null != keys && keys.size() > 0) {
			for (String key : keys)
				queryPart.append(objectVariableName).append(CypherQueryConfigurationConstants.DOT).append(key)
						.append(CypherQueryConfigurationConstants.EQUALS).append("null")
						.append(CypherQueryConfigurationConstants.COMMA);
		}
		return StringUtils.removeEnd(queryPart.toString(), CypherQueryConfigurationConstants.COMMA);
	}

	protected static String getStringValue(Object object) {
		String str = "";
		if (null != object) {
			if (object.getClass().isArray()) {
				str = Arrays.toString((String[]) object);
			} else {
				str = String.valueOf(object);
			}
		}
		return str;
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

	@SuppressWarnings("unused")
	private static String mapToString(Map<String, Object> map) {
		StringBuilder stringBuilder = new StringBuilder();

		for (String key : map.keySet()) {
			String value = getStringValue(map.get(key));
			try {
				stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
				stringBuilder.append(":");
				stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}

		return stringBuilder.toString();
	}
}
