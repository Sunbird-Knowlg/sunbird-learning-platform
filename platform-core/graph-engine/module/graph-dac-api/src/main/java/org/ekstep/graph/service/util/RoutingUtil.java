package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.GraphOperation;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.common.Platform;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;

public class RoutingUtil {

	public static String getRoute(String graphId, GraphOperation graphOperation) {
		PlatformLogger.log("Graph Id: ", graphId);

		// Checking Graph Id for 'null' or 'Empty'
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Graph Id: " + graphId + "]");

		String routeUrl = "bolt://localhost:7687";
		try {
			String baseKey = DACConfigurationConstants.DEFAULT_ROUTE_PROP_PREFIX + StringUtils.lowerCase(graphOperation.name())
							+ DACConfigurationConstants.DEFAULT_PROPERTIES_NAMESPACE_SEPARATOR;

			System.out.println("BaseKey: " + baseKey);
			System.out.println("Config : " + Platform.config.hasPath(baseKey + graphId));
			if (Platform.config.hasPath(baseKey + graphId)) {
				routeUrl = Platform.config.getString(baseKey + graphId);
			} else if (Platform.config.hasPath(baseKey + DACConfigurationConstants.DEFAULT_NEO4J_BOLT_ROUTE_ID)) {
				routeUrl = Platform.config.getString(baseKey + DACConfigurationConstants.DEFAULT_NEO4J_BOLT_ROUTE_ID);
			} else {
				PlatformLogger.log("Graph connection configuration not defined.", LoggerEnum.WARN.name());
			}
			PlatformLogger.log("Request path for graph: " + graphId + " | URL: " + routeUrl);
		} catch (Exception e) {
			PlatformLogger.log("Error fetching location from graph.properties", null, e);
		}
		System.out.println("routeUrl" + routeUrl);
		return routeUrl;
	}
}
