package org.ekstep.cassandra.connector.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.net.InetSocketAddress;
import java.util.*;

public class CassandraConnector {

	/** Cassandra Cluster. */
	private static Cluster cluster;

	/** Cassandra Session. */
	private static Session session;

	private static Map<String,Session> sessionMap=new HashMap<>();
	
	static {
		prepareSession("lp","lpa");
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
		return getSession("lp");
	}

	/**
	 * @param sessionKey
	 * @return
	 */
	public static Session getSession(String sessionKey) {
		Session session = null;
		if (sessionMap.containsKey(sessionKey)) {
			session = sessionMap.get(sessionKey);
			if (null == session || session.isClosed()) {
				prepareSession(sessionKey);
				session = sessionMap.get(sessionKey);
			}
		} else {
			prepareSession(sessionKey);
			session = sessionMap.get(sessionKey);
		}

		if (null != session)
			return session;
		else
			throw new ServerException("ERR_INITIALISE", "Error while initialising cassandra");
	}

	/**
	 * @param sessionKeys
	 */
	private static void prepareSession(String... sessionKeys) {
		for (String sessionKey : sessionKeys) {
			List<String> connectionInfo = getConnectionInfo(sessionKey);
			List<InetSocketAddress> addressList = getSocketAddress(connectionInfo);
			try {
				cluster = Cluster.builder().addContactPointsWithPorts(addressList).build();
				session = cluster.connect();
				sessionMap.put(sessionKey, session);
				registerShutdownHook();
			} catch (Exception e) {
				TelemetryManager.error("Error! While Loading Cassandra Properties." + e.getMessage(), e);
			}
		}
	}

	/**
	 *
	 * @param sessionKey
	 * @return
	 */
	private static List<String> getConnectionInfo(String sessionKey) {
		List<String> connectionInfo = null;
		if (sessionKey.equalsIgnoreCase("lp")) {
			connectionInfo = Arrays.asList(Platform.config.getString("cassandra.lp.connection").split(","));
		} else if (sessionKey.equalsIgnoreCase("lpa")) {
			connectionInfo = Arrays.asList(Platform.config.getString("cassandra.lpa.connection").split(","));
		}
		if (null == connectionInfo || connectionInfo.isEmpty())
			connectionInfo = new ArrayList<>(Arrays.asList("localhost:9042"));

		return connectionInfo;
	}

	/**
	 *
	 * @param hosts
	 * @return
	 */
	private static List<InetSocketAddress> getSocketAddress(List<String> hosts) {
		List<InetSocketAddress> connectionList = new ArrayList<>();
		for (String connection : hosts) {
			String[] conn = connection.split(":");
			String host = conn[0];
			int port = Integer.valueOf(conn[1]);
			connectionList.add(new InetSocketAddress(host, port));
		}
		return connectionList;
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
