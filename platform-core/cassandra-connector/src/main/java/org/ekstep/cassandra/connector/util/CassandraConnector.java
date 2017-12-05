package org.ekstep.cassandra.connector.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.logger.LoggerEnum;
import org.ekstep.common.logger.PlatformLogger;

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
			PlatformLogger.log("Fetching cassandra properties from configPath" + host + port, null, LoggerEnum.INFO.name());
			if (StringUtils.isBlank(host))
				host = "localhost";					
			if (port <= 0)
				port = 9042;
			cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
			session = cluster.connect();
			registerShutdownHook();
		} catch (Exception e) {
			PlatformLogger.log("Error! While Loading Cassandra Properties.", e.getMessage(), e);
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
				PlatformLogger.log("Shutting down Cassandra connector session");
				CassandraConnector.close();
			}
		});
	}
	
}
