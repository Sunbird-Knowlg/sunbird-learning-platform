package org.ekstep.graph.service.util;

import java.util.Map;

import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.service.common.Neo4JOperation;

public class QueryUtil {

	public static String getQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		PlatformLogger.log("Neo4J Operation: " + operation);
//		PlatformLogger.log("Parameter Map: ", parameterMap);

		String query = "";
		try {
			query = CypherUtil.getQuery(operation, parameterMap);
		} catch (Exception e) {
			PlatformLogger.log("Error! While Generating the Query.", null, e);
		}
		PlatformLogger.log("Returning Query: " + query + "");
		return query;
	}
}
