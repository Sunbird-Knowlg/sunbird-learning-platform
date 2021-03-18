package org.sunbird.graph.service.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.service.common.DACConfigurationConstants;
import org.sunbird.graph.service.common.GraphOperation;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class DriverUtil {

	private static Map<String, Driver> driverMap = new HashMap<String, Driver>();

	public static Driver getDriver(String graphId, GraphOperation graphOperation) {
		TelemetryManager.log("Get Driver for Graph Id: "+ graphId);
		String driverKey = graphId + DACConfigurationConstants.UNDERSCORE
				+ StringUtils.lowerCase(graphOperation.name());
		TelemetryManager.log("Driver Configuration Key: " + driverKey);

		Driver driver = driverMap.get(driverKey);
		if (null == driver) {
			driver = loadDriver(graphId, graphOperation);
			driverMap.put(driverKey, driver);
		}
		return driver;
	}

	private static Driver loadDriver(String graphId, GraphOperation graphOperation) {
		String route = RoutingUtil.getRoute(graphId, graphOperation);
		Driver driver = GraphDatabase.driver(route, getConfig());
		if (null != driver)
			registerShutdownHook(driver);
		return driver;
	}

	private static void registerShutdownHook(Driver driver) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (null != driver)
					driver.close();
			}
		});
	}

	public static Config getConfig() {
		Config.ConfigBuilder config = Config.build();
		config.withEncryptionLevel(Config.EncryptionLevel.NONE);
		config.withMaxIdleSessions(DACConfigurationConstants.NEO4J_SERVER_MAX_IDLE_SESSION);
		config.withTrustStrategy(Config.TrustStrategy.trustAllCertificates());
		return config.toConfig();
	}
	public static void closeDrivers() {
		for (Iterator<Map.Entry<String, Driver>> it = driverMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Driver> entry = it.next();
			Driver driver = entry.getValue();
			driver.close();
			it.remove();
		}
	}
}
