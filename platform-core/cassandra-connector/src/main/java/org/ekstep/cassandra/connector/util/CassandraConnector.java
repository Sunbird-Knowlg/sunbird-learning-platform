package org.ekstep.cassandra.connector.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraConnector {

	/** Cassandra Cluster. */
	private static Cluster cluster;

	/** Cassandra Session. */
	private static Session session;
	
	static {
		loadProperties();
	}
	
	public static void loadProperties(){
		try{
			String host = Platform.config.getString("cassandra.host");
			int port = Platform.config.getInt("cassandra.port");
			TelemetryManager.info("Fetched cassandra properties from config - Path: " + host +":"+ port);
			if (StringUtils.isBlank(host))
				host = "localhost";					
			if (port <= 0)
				port = 9042;
			cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
			session = cluster.connect();
			registerShutdownHook();
		} catch (Exception e) {
			TelemetryManager.error("Error! While Loading Cassandra Properties." + e.getMessage(), e);
		}
	}

	/**
	 * Provide my Session.
	 * 
	 * @return My session.
	 */
	public static Session getSession() {
		return session;
	}

	/**
	 * Close connection with the cluster.
	 * 
	 */
	public static void close() {
		session.close();
		cluster.close();
	}

	/**
	 * Register JVM shutdown hook to close cassandra open session.
	 */
	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				TelemetryManager.log("Shutting down Cassandra connector session");
				CassandraConnector.close();
			}
		});
	}
	
}
