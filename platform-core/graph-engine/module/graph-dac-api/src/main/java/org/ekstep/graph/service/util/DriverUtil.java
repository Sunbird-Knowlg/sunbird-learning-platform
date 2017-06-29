package org.ekstep.graph.service.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.GraphOperation;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.ClientException;

public class DriverUtil {

	private static Logger LOGGER = LogManager.getLogger(DriverUtil.class.getName());
	private static Map<String, Driver> driverMap = new HashMap<String, Driver>();
	
	public static Driver getDriver(String graphId, GraphOperation graphOperation) {
		LOGGER.debug("Get Driver for Graph Id: ", graphId);
		String driverKey = graphId + DACConfigurationConstants.DEFAULT_GRAPH_ID_AND_GRAPH_OPERATION_SEPARATOR + StringUtils.lowerCase(graphOperation.name());
		Driver driver = driverMap.get(driverKey);
		if (null == driver) {
			driver = loadDriver(graphId, graphOperation);
			driverMap.put(driverKey, driver);
		}
		return driver;
	}

	public static Driver loadDriver(String graphId, GraphOperation graphOperation) {
		LOGGER.debug("Loading driver for Graph Id: ", graphId);
		String driverType = DACConfigurationConstants.NEO4J_SERVER_DRIVER_TYPE;
		if (StringUtils.isBlank(driverType))
			throw new ClientException(DACErrorCodeConstants.INVALID_DRIVER.name(),
					DACErrorMessageConstants.INVALID_DRIVER_TYPE + " | [Driver Initialization Failed.]");
		LOGGER.debug("Driver Type: " + driverType);
		
		Driver driver = null;
		switch (driverType) {
		case "simple":
		case "SIMPLE":
			LOGGER.debug("Reading Simple Driver. | [Driver Initialization.]");
			driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId, graphOperation), ConfigUtil.getConfig());
			break;
			
		case "medium":
		case "MEDIUM":
			LOGGER.debug("Reading Medium Driver. | [Driver Initialization.]");
			driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId, graphOperation), AuthTokenUtil.getAuthToken(), ConfigUtil.getConfig());
			break;
			
		case "complex":
		case "COMPLEX":
			LOGGER.debug("Reading Complex Driver. | [Driver Initialization.]");
			driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId, graphOperation), AuthTokenUtil.getAuthToken(),
					ConfigUtil.getConfig());
			break;

		default:
			LOGGER.debug("Invalid Database (Bolt) Driver Type: " + driverType + " | [Default Driver Type is ]");
			driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId, null));
			break;
		}
		if (null != driver)
			registerShutdownHook(driver);
		return driver;
	}
	
	private static void registerShutdownHook(Driver driver) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Closing Neo4j Graph Driver...");
                if (null != driver)
                	driver.close();
            }
        });
    }

}
