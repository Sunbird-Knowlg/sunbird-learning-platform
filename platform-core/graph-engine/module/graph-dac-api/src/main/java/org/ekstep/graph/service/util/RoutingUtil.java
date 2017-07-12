package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.GraphOperation;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.mgr.Configuration;

public class RoutingUtil {

	public static String getRoute(String graphId, GraphOperation graphOperation) {
		PlatformLogger.log("Graph Id: ", graphId);

		// Checking Graph Id for 'null' or 'Empty'
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Graph Id: " + graphId + "]");

		String routeUrl = "bolt://localhost:7687";
		try {
			String path = Configuration.getProperty(
					DACConfigurationConstants.DEFAULT_ROUTE_PROP_PREFIX + StringUtils.lowerCase(graphOperation.name())
							+ DACConfigurationConstants.DEFAULT_PROPERTIES_NAMESPACE_SEPARATOR + graphId);
			PlatformLogger.log("Request path for graph: " + graphId + " | URL: " + path);
			if (StringUtils.isBlank(path)) {
				path = Configuration.getProperty(DACConfigurationConstants.DEFAULT_ROUTE_PROP_PREFIX
						+ StringUtils.lowerCase(graphOperation.name())
						+ DACConfigurationConstants.DEFAULT_PROPERTIES_NAMESPACE_SEPARATOR
						+ DACConfigurationConstants.DEFAULT_NEO4J_BOLT_ROUTE_ID);
				PlatformLogger.log("Using default graph path for " + graphId + " | URL: " + path);
			}
			if (StringUtils.isNotBlank(path))
				routeUrl = path;
		} catch (Exception e) {
			PlatformLogger.log("Error fetching location from graph.properties", null, e);
		}
		return routeUrl;
	}

}
