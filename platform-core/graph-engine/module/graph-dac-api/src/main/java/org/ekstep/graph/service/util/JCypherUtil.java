package org.ekstep.graph.service.util;

import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.common.Identifier;
import org.ekstep.graph.dac.enums.AuditProperties;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.Neo4JOperation;
import org.neo4j.driver.v1.exceptions.ClientException;

import iot.jcypher.query.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.factories.clause.ON_CREATE;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;

public class JCypherUtil {

	public static String getQuery(Neo4JOperation operation, Node node) {

		PlatformLogger.log("Validating Database (Neo4J) Operation against 'null'.");
		if (null == operation)
			throw new ClientException(DACErrorCodeConstants.INVALID_OPERATION.name(),
					DACErrorMessageConstants.INVALID_OPERATION + " | [Query Generation Failed.]");

		PlatformLogger.log("Validating Graph Engine Node against 'null'.");
		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Query Generation Failed.]");

		String query = "";
		query = generateQuery(operation, node);
		return query;

	}

	private static String generateQuery(Neo4JOperation operation, Node node) {
		JcNode jcNode = getJcNode(node.getIdentifier());
		return getCypherQuery(operation, node, jcNode);
	}

	private static JcNode getJcNode(String identifier) {
		if (StringUtils.isBlank(identifier))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | [Node Creation Failed.]");

		return new JcNode(identifier);
	}

	private static String getCypherQuery(Neo4JOperation operation, Node node, JcNode jcNode) {
		String query = "";

		if (null != jcNode && null != node) {
			JcQuery jcQuery = new JcQuery();
			String opt = operation.name();
			switch (opt) {
			case "CREATE_NODE":
				jcQuery.setClauses(new IClause[] { getClause(CREATE.node(jcNode).label(node.getGraphId()), node) });
				query = Util.toCypher(jcQuery, Format.PRETTY_3);
				break;
			case "UPSERT_NODE":
				jcQuery.setClauses(new IClause[] { getClause(MERGE.node(jcNode).label(node.getGraphId()), node) });
				query = Util.toCypher(jcQuery, Format.PRETTY_3);
				ON_CREATE.SET(jcNode.property("")).to(null);
				break;
			case "UPDATE_NODE":

				break;
			case "SEARCH_NODE":

				break;
			case "CREATE_UNIQUE":

				break;
			case "CREATE_INDEX":

				break;

			default:
				break;
			}
		}

		return query;
	}
	
	private static IClause getClause(iot.jcypher.query.api.pattern.Node patternNode, Node node) {
		PlatformLogger.log("JCypher Pattern Node: ", patternNode);
		PlatformLogger.log("Graph Engine Node: ", node);

		String date = DateUtils.formatCurrentDate();
		PlatformLogger.log("Date: " + date);

		if (null == patternNode)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_PATTERN_NODE + " | [Metadata generation Failed.]");

		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Metadata generation Failed.]");

		if (null != node.getMetadata() && !node.getMetadata().isEmpty())
			for (Entry<String, Object> entry : node.getMetadata().entrySet())
				patternNode.property(entry.getKey()).value(entry.getValue());

		patternNode = addSystemMetadata(patternNode, node, date);

		return patternNode;
	}

	private static iot.jcypher.query.api.pattern.Node addSystemMetadata(iot.jcypher.query.api.pattern.Node patternNode,
			Node node, String date) {
		PlatformLogger.log("Pattern Node: ", patternNode);
		PlatformLogger.log("Graph Engine Node: ", node);
		PlatformLogger.log("Date: ", date);

		try {
			if (null != patternNode && null != node && null != date) {
				// Setting Identifier
				PlatformLogger.log("Setting System Metadata.");
				if (StringUtils.isBlank(node.getIdentifier()))
					node.setIdentifier(Identifier.getIdentifier(node.getGraphId(), Identifier.getUniqueIdFromTimestamp()));

				// Setting Identifier and Node Type
				PlatformLogger.log("Setting System Properties.");
				patternNode.property(SystemProperties.IL_UNIQUE_ID.name()).value(node.getIdentifier());
				patternNode.property(SystemProperties.IL_SYS_NODE_TYPE.name()).value(node.getNodeType());

				// Setting Object Type
				if (StringUtils.isNotBlank(node.getObjectType()))
					patternNode.property(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).value(node.getObjectType());
			}
		} catch (Exception e) {
			throw new ClientException(DACErrorCodeConstants.SYSTEM_METADATA.name(),
					DACErrorMessageConstants.SYSTEM_METADATA_CREATION_ERROR + " | [System Metadata Creation Failed.]");
		}
		return patternNode;
	}

	@SuppressWarnings("unused")
	private static iot.jcypher.query.api.pattern.Node addAuditMetadata(iot.jcypher.query.api.pattern.Node patternNode,
			Node node, String date, boolean isUpdateOnly) {
		PlatformLogger.log("Pattern Node: ", patternNode);
		PlatformLogger.log("Graph Engine Node: ", node);
		PlatformLogger.log("Date: ", date);
		PlatformLogger.log("Is Update Opertion ? ", isUpdateOnly);

		try {
			// Setting Audit Properties
			PlatformLogger.log("Setting Audit Properties.");
			if (BooleanUtils.isFalse(isUpdateOnly))
				patternNode.property(AuditProperties.createdOn.name()).value(date);
			patternNode.property(AuditProperties.lastUpdatedOn.name()).value(date);
		} catch (Exception e) {
			throw new ClientException(DACErrorCodeConstants.SYSTEM_METADATA.name(),
					DACErrorMessageConstants.SYSTEM_METADATA_CREATION_ERROR + " | [System Metadata Creation Failed.]");
		}

		return patternNode;
	}

	@SuppressWarnings("unused")
	private static iot.jcypher.query.api.pattern.Node addVersionKey(iot.jcypher.query.api.pattern.Node patternNode,
			Node node, String date) {
		PlatformLogger.log("Pattern Node: ", patternNode);
		PlatformLogger.log("Graph Engine Node: ", node);
		PlatformLogger.log("Date: ", date);

		try {
			if (StringUtils.isNotBlank(date)) {
				// Setting Version Key
				PlatformLogger.log("Setting 'versionKey'.");
				patternNode.property(GraphDACParams.versionKey.name())
						.value(Long.toString(DateUtils.parse(date).getTime()));

				node.getMetadata().put(GraphDACParams.versionKey.name(),
						Long.toString(DateUtils.parse(date).getTime()));
			}
		} catch (Exception e) {
			throw new ClientException(DACErrorCodeConstants.SYSTEM_METADATA.name(),
					DACErrorMessageConstants.SYSTEM_METADATA_CREATION_ERROR + " | [System Metadata Creation Failed.]");
		}

		return patternNode;
	}
}
