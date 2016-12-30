package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.graph.common.mgr.Configuration;

public class RoutingUtil {

	private static Logger LOGGER = LogManager.getLogger(RoutingUtil.class.getName());
	
	private static final String ROUTE_PROP_PREFIX = "route.";
	private static final String DEFAULT_ROUTE_ID = "all";

	public static String getRoute(String graphId) {
		LOGGER.debug("Graph Id: ", graphId);

		// Checking Graph Id for 'null' or 'Empty'
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Graph Id: " + graphId + "]");
		
		String routeUrl = "bolt://localhost:7687";
		try {
			String path = Configuration.getProperty(ROUTE_PROP_PREFIX + graphId);
			LOGGER.info("Request path for graph: " + graphId + " | URL: " + path);
			if (StringUtils.isBlank(path)) {
				path = Configuration.getProperty(ROUTE_PROP_PREFIX + DEFAULT_ROUTE_ID);
				LOGGER.info("Using default graph path for " + graphId + " | URL: " + path);
			}
			if (StringUtils.isNotBlank(path))
				routeUrl = path;
		} catch (Exception e) {
			LOGGER.error("Error fetching location from graph.properties", e);
		}
		return routeUrl;
	}

}
