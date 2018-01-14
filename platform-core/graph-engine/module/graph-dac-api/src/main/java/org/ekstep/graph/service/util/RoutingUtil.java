package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.GraphOperation;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.exceptions.ClientException;

public class RoutingUtil {

	public static String getRoute(String graphId, GraphOperation graphOperation) {

		// Checking Graph Id for 'null' or 'Empty'
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Graph Id: " + graphId + "]");

		String routeUrl = "bolt://localhost:7687";
		try {
			String baseKey = DACConfigurationConstants.DEFAULT_ROUTE_PROP_PREFIX + StringUtils.lowerCase(graphOperation.name())
					+ DACConfigurationConstants.DOT;
			if (Platform.config.hasPath(baseKey + graphId)) {
				routeUrl = Platform.config.getString(baseKey + graphId);
			} else if (Platform.config.hasPath(baseKey + DACConfigurationConstants.DEFAULT_NEO4J_BOLT_ROUTE_ID)) {
				routeUrl = Platform.config.getString(baseKey + DACConfigurationConstants.DEFAULT_NEO4J_BOLT_ROUTE_ID);
			} else {
				TelemetryManager.warn("Graph connection configuration not defined.");
			}
			TelemetryManager.log("Request path for graph: " + graphId + " | URL: " + routeUrl);
		} catch (Exception e) {
			TelemetryManager.error("Error fetching location from graph.properties", e);
		}
		return routeUrl;
	}
}
