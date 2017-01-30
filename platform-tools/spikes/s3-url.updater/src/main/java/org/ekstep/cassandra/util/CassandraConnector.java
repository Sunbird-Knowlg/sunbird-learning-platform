package org.ekstep.cassandra.util;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraConnector {

	private static Cluster cluster;
	private static Session session;

	public static void init(String host) {
		cluster = Cluster.builder().addContactPoint(host).withPort(9042).build();
		session = cluster.connect();
	}

	public static Session getSession() {
		return session;
	}

	public static void close() {
		session.close();
		cluster.close();
	}
}
