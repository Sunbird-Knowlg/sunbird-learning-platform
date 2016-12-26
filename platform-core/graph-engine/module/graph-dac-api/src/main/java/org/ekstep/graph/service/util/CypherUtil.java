package org.ekstep.graph.service.util;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.Neo4JOperation;
import org.neo4j.driver.v1.exceptions.ClientException;

public class CypherUtil {

	private static Logger LOGGER = LogManager.getLogger(JCypherUtil.class.getName());

	public static String getQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		LOGGER.info("Validating Database (Neo4J) Operation against 'null'.");
		if (null == operation)
			throw new ClientException(DACErrorCodeConstants.INVALID_OPERATION.name(),
					DACErrorMessageConstants.INVALID_OPERATION + " | [Query Generation Failed.]");

		LOGGER.info("Validating Graph Engine Node against 'null'.");
		if (null == parameterMap)
			throw new ClientException(DACErrorCodeConstants.INVALID_PARAMETER.name(),
					DACErrorMessageConstants.INVALID_PARAMETER_MAP + " | [Query Generation Failed.]");

		String query = "";
		query = generateQuery(operation, parameterMap);
		return query;

	}

	private static String generateQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		String query = "";
		if (null != operation && null != parameterMap && !parameterMap.isEmpty())
			query = getCypherQuery(operation, parameterMap);

		LOGGER.info("Returning Generated Cypher Query: " + query);
		return query;
	}

	private static String getCypherQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		String query = "";
		if (null != operation && null != parameterMap) {
			String opt = operation.name();
			switch (opt) {
			case "CREATE_NODE":
				query = NodeQueryGenerationUtil.generateCreateNodeCypherQuery(parameterMap);
				break;
			case "UPSERT_NODE":
				query = NodeQueryGenerationUtil.generateUpsertNodeCypherQuery(parameterMap);
				break;
			case "UPDATE_NODE":
				query = NodeQueryGenerationUtil.generateUpdateNodeCypherQuery(parameterMap);
				break;
			case "IMPORT_NODES":
				query = NodeQueryGenerationUtil.generateUpdateNodeCypherQuery(parameterMap);
				break;
			case "UPDATE_PROPERTY":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "UPDATE_PROPERTIES":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "REMOVE_PROPERTY":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "REMOVE_PROPERTIES":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "DELETE_NODE":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "UPSERT_ROOT_NODE":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "CREATE_UNIQUE_CONSTRAINT":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "CREATE_INDEX":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "DELETE_GRAPH":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "CREATE_RELATION":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "UPDATE_RELATION":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "DELETE_RELATION":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "CREATE_INCOMING_RELATIONS":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "CREATE_OUTGOING_RELATIONS":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "DELETE_INCOMING_RELATIONS":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "DELETE_OUTGOING_RELATIONS":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "REMOVE_RELATION_METADATA":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "CREATE_COLLECTION":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "DELETE_COLLECTION":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "IMPORT_GRAPH":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_NODE_BY_ID":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_NODE_BY_UNIQUE_ID":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_NODES_BY_PROPERTY":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_NODES_BY_SEARCH_CRITERIA":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_NODE_PROPERTY":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_ALL_NODES":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_ALL_RELATIONS":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_RELATION_PROPERTY":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_RELATION":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "CHECK_CYCLIC_LOOP":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "EXECUTE_QUERY":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "SEARCH_NODES":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_NODES_COUNT":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "TRAVERSE":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "TRAVERSE_SUB_GRAPH":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "GET_SUB_GRAPH":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;

			default:
				LOGGER.warn("Invalid Neo4J Operation !");
				break;
			}
		}

		LOGGER.info("Returning Cypher Query For Operation - " + operation.name() + " | Query - " + query);
		return query;
	}

}
