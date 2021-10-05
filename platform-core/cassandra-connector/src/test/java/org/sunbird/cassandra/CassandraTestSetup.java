package org.sunbird.cassandra;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.datastax.driver.core.Session;

public class CassandraTestSetup {

	private static Session session = null;

	@AfterClass
	public static void afterTest() throws Exception {
		tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() throws Exception {
		setupEmbeddedCassandra();
	}

	protected static Session getSession() {
		return session;
	}

	private static void setupEmbeddedCassandra() throws Exception {
		EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
		session = CassandraConnector.getSession();
	}

	private static void tearEmbeddedCassandraSetup() {
		try {
			if (!session.isClosed())
				session.close();
			EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected static void executeScript(String... querys) throws Exception {
		session = CassandraConnector.getSession();
		for (String query : querys) {
			session.execute(query);
		}
	}

}
