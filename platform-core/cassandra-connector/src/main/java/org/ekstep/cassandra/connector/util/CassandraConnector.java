package org.ekstep.cassandra.connector.util;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.net.InetSocketAddress;
import java.util.*;

public class CassandraConnector {

	/** Cassandra Session Map. */
	private static Map<String,Session> sessionMap=new HashMap<>();

    static {
        prepareSession("lp");
    }

	/**
	 * Provide lp Session.
	 *
	 * @return lp session.
	 */
	public static Session getSession() {
		return getSession("lp");
	}

	/**
	 * @param sessionKey
	 * @return
	 */
    public static Session getSession(String sessionKey) {
        Session session = sessionMap.containsKey(sessionKey) ? sessionMap.get(sessionKey) : null;

        if (null == session || session.isClosed()) {
            prepareSession(sessionKey);
            session = sessionMap.get(sessionKey);
        }
        if (null == session)
            throw new ServerException("ERR_INITIALISE_CASSANDRA_SESSION", "Error while initialising cassandra");
        return session;
    }

	/**
	 * @param sessionKeys
	 */
    private static void prepareSession(String... sessionKeys) {
		ConsistencyLevel consistencyLevel = getConsistencyLevel();
        for (String sessionKey : sessionKeys) {
            List<String> connectionInfo = getConnectionInfo(sessionKey.toLowerCase());
            List<InetSocketAddress> addressList = getSocketAddress(connectionInfo);
            try {
				if (null != consistencyLevel) {
					sessionMap.put(sessionKey.toLowerCase(), Cluster.builder().addContactPointsWithPorts(addressList).withQueryOptions(new QueryOptions().setConsistencyLevel(consistencyLevel)).build().connect());
				} else {
					sessionMap.put(sessionKey.toLowerCase(), Cluster.builder().addContactPointsWithPorts(addressList).build().connect());
				}

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
		} else if (sessionKey.equalsIgnoreCase("sunbird")) {
			connectionInfo = Arrays.asList(Platform.config.getString("cassandra.sunbird.connection").split(","));
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
        sessionMap.entrySet().stream().forEach(stream -> stream.getValue().close());
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

	/**
	 * This Method Returns the value of Consistency Level for Multi Node/DC Cassandra Cluster.
	 * @return ConsistencyLevel
	 */
	private static ConsistencyLevel getConsistencyLevel() {
		String consistencyLevel = Platform.config.hasPath("cassandra.consistency.level") ?
				Platform.config.getString("cassandra.consistency.level") : null;
		if (StringUtils.isNotBlank(consistencyLevel))
			return ConsistencyLevel.valueOf(consistencyLevel.toUpperCase());
		return null;
	}

}
