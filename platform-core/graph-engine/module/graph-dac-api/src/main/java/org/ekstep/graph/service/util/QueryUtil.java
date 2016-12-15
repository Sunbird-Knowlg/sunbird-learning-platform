package org.ekstep.graph.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.Neo4JOperation;

import com.ilimi.graph.dac.model.Node;

public class QueryUtil {
	
	private static Logger LOGGER = LogManager.getLogger(QueryUtil.class.getName());
	
	public static String getQuery(Neo4JOperation operation, Node node) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Graph Engine Node: ", node);
		
		String query = "";
		try {
			
		} catch(Exception e) {
			LOGGER.error("Error! While Generating the Query.", e);
		}
		return query;
	}

}
