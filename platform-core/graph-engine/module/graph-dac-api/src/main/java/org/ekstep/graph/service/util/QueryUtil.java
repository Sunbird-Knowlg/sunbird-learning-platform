package org.ekstep.graph.service.util;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.Neo4JOperation;

public class QueryUtil {

	private static Logger LOGGER = LogManager.getLogger(QueryUtil.class.getName());

	public static String getQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		String query = "";
		try {
			query = CypherUtil.getQuery(operation, parameterMap);
		} catch (Exception e) {
			LOGGER.error("Error! While Generating the Query.", e);
		}
		LOGGER.debug("Returning Query: " + query + "");
		return query;
	}
}
