package org.sunbird.jobs.samza.util;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import org.apache.samza.config.Config;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CassandraConnector {
    private Session session = null;
    
    public CassandraConnector(Config config) {
        List<String> connectionInfo = Arrays.asList(config.get("cassandra.connection.platform_courses", "localhost:9042").split(","));
        List<InetSocketAddress> addresses = getSocketAddress(connectionInfo);
        session = Cluster.builder().addContactPointsWithPorts(addresses).withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM)).build().connect();
    }

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

    public Session getSession() {
        return session;
    }
}
