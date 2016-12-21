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
				query = QueryGenerationUtil.generateCreateNodeCypherQuery(parameterMap);
				break;
			case "UPSERT_NODE":
				query = QueryGenerationUtil.generateUpsertNodeCypherQuery(parameterMap);
				break;
			case "UPDATE_NODE":
				query = QueryGenerationUtil.generateUpdateNodeCypherQuery(parameterMap);
				break;
			case "UPDATE_PROPERTY":
				query = QueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "UPDATE_PROPERTIES":
				query = QueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "REMOVE_PROPERTY":
				query = QueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "REMOVE_PROPERTIES":
				query = QueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "DELETE_NODE":
				query = QueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "UPSERT_ROOTNODE":
				query = QueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "SEARCH_NODE":
				query = QueryGenerationUtil.generateSearchNodeCypherQuery(parameterMap);
				break;
			case "CREATE_UNIQUE":
				query = QueryGenerationUtil.generateCreateUniqueConstraintCypherQuery(parameterMap);
				break;
			case "CREATE_INDEX":
				query = QueryGenerationUtil.generateCreateIndexCypherQuery(parameterMap);
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
