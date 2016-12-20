package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.driver.v1.exceptions.ClientException;

public class RoutingUtil {

	private static Logger LOGGER = LogManager.getLogger(RoutingUtil.class.getName());

	public static String getRoute(String graphId) {
		LOGGER.debug("Graph Id: ", graphId);

		// Checking Graph Id for 'null' or 'Empty'
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Graph Id: " + graphId + "]");
		
		String routeUrl = "bolt://localhost";
		
		return routeUrl;
	}

}
