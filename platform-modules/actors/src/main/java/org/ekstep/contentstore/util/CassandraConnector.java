package org.ekstep.contentstore.util;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.mgr.Configuration;

/**
 * Class used for connecting to Cassandra database.
 * 
 * @author rayulu
 */
public class CassandraConnector {

	private static Properties props;
	
	/** Cassandra Cluster. */
	private static Cluster cluster;

	/** Cassandra Session. */
	private static Session session;

	static {
		// Connect to Cassandra Cluster specified by provided node IP address
		// and port number in cassandra.properties file
		try (InputStream inputStream = Configuration.class.getClassLoader()
				.getResourceAsStream("cassandra.properties")) {
			if (null != inputStream) {
				props = new Properties();
				props.load(inputStream);
				String host = props.getProperty("cassandra.host");
				if (StringUtils.isBlank(host))
					host = "localhost";
				int port = -1;
				String portConfig = props.getProperty("cassandra.port");
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
		} catch (Exception e) {
			PlatformLogger.log("Error! While Loading Cassandra Properties.", e.getMessage(), e);
		}
	}
	
	// method to load cassandra properties for samza job
	public static void loadProperties(Map<String, Object> props) {
		props.putAll(props);
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
