package org.sunbird.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.graph.service.common.DACConfigurationConstants;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.graph.service.common.GraphOperation;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.exceptions.ClientException;

public class RoutingUtil {

	public static String getRoute(String graphId, GraphOperation graphOperation) {

		// Checking Graph Id for 'null' or 'Empty'
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Graph Id: " + graphId + "]");

		String routeUrl = "bolt://localhost:7687";
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
		return routeUrl;
	}
}
