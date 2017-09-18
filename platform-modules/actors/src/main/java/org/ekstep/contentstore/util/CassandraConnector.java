package org.ekstep.contentstore.util;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.ilimi.common.Platform;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;

/**
 * Class used for connecting to Cassandra database.
 * 
 * @author rayulu, rashmi
 */
public class CassandraConnector {

	private static Properties props = new Properties();

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
	
	
	// method to load cassandra properties for samza job
	public static void loadProperties(Map<String, Object> prop) {
		props.putAll(prop);
		String host = (String)props.get("cassandra.host");
		if (StringUtils.isBlank(host))
			host = "localhost";
		int port = -1;
		String portConfig = (String)props.get("cassandra.port");
		if (StringUtils.isNotBlank(portConfig)) {
			try {
				port = Integer.parseInt(portConfig);
			} catch (Exception e) {
			}
		}
		if (port < 0)
			port = 9042;
		cluster = Cluster.builder().addContactPoint(host).withPort(port).build();
		session = cluster.connect();
		registerShutdownHook();
	}
	
	public static void loadProperties(Properties props) {
		props.putAll(props);
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
