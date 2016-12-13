package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.ClientException;

public class DriverUtil {

	private static Logger LOGGER = LogManager.getLogger(DriverUtil.class.getName());

	public static Driver getDriver(String graphId) {
		LOGGER.debug("Graph Id: ", graphId);

		String driverType = DACConfigurationConstants.NEO4J_SERVER_DRIVER_TYPE;
		if (StringUtils.isBlank(driverType))
			throw new ClientException(DACErrorCodeConstants.INVALID_DRIVER.name(),
					DACErrorMessageConstants.INVALID_DRIVER_TYPE + " | [Driver Initialization Failed.]");
		LOGGER.info("Driver Type: " + driverType);
		
		Driver driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId));
		
		switch (driverType) {
		case "simple":
		case "SIMPLE":
			LOGGER.info("Reading Simple Driver. | [Driver Initialization.]");
			driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId));
			break;
			
		case "medium":
		case "MEDIUM":
			LOGGER.info("Reading Medium Driver. | [Driver Initialization.]");
			driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId), AuthTokenUtil.getAuthToken());
			break;
			
		case "complex":
		case "COMPLEX":
			LOGGER.info("Reading Complex Driver. | [Driver Initialization.]");
			driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId), AuthTokenUtil.getAuthToken(),
					ConfigUtil.getConfig());
			break;

		default:
			LOGGER.info("Invalid Database (Bolt) Driver Type: " + driverType + " | [Default Driver Type is ]");
			break;
		}

		return driver;
	}

}
