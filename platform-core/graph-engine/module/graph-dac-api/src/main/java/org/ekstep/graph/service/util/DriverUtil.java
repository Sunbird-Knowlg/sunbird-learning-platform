package org.ekstep.graph.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class DriverUtil {
	
	private static Logger LOGGER = LogManager.getLogger(DriverUtil.class.getName());
	
	public static Driver getDriver(String graphId) {
		Driver driver = GraphDatabase.driver(RoutingUtil.getRoute(graphId), AuthTokenUtil.getAuthToken(), ConfigUtil.getConfig());
		
		return driver;
	}

}
