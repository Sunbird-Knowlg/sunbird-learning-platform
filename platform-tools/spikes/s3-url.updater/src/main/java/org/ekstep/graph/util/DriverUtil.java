package org.ekstep.graph.util;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Config.ConfigBuilder;
import org.neo4j.driver.v1.Config.EncryptionLevel;
import org.neo4j.driver.v1.Config.TrustStrategy;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class DriverUtil {

	private static Map<String, Driver> driverMap = new HashMap<String, Driver>();

	public static Driver getDriver(String path) {
		Driver driver = driverMap.get(path);
		if (null == driver) {
			driver = loadDriver(path);
			driverMap.put(path, driver);
		}
		return driver;
	}

	public static Driver loadDriver(String path) {
		Driver driver = null;
		driver = GraphDatabase.driver(getRoute(path), getConfig());
		if (null != driver)
			registerShutdownHook(driver);
		return driver;
	}

	public static String getRoute(String url) {
		String routeUrl = "bolt://" + url;
		return routeUrl;
	}

	public static Config getConfig() {
		ConfigBuilder config = Config.build();
		config.withEncryptionLevel(EncryptionLevel.NONE);
		config.withMaxIdleSessions(20);
		config.withTrustStrategy(getTrustStrategy());
		return config.toConfig();
	}

	private static TrustStrategy getTrustStrategy() {
		TrustStrategy trustStrategy = TrustStrategy.trustAllCertificates();
		return trustStrategy;
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
