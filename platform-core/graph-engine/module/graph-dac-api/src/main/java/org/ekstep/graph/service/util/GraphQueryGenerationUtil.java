package org.ekstep.graph.service.util;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphQueryGenerationUtil {
	
	private static Logger LOGGER = LogManager.getLogger(GraphQueryGenerationUtil.class.getName());
	
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
	
	public static String generateDeleteGraphCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			// Sample
			// MATCH (n)
			// REMOVE n:Person
		}

		LOGGER.info("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

}
