package org.ekstep.dialcode.test.common;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.datastax.driver.core.Session;
import org.ekstep.cassandra.connector.util.CassandraConnector;

/**
 * This Class will allow setting Up Embedded Cassandra and execute script for
 * Unit Test Cases.
 * 
 * @author gauraw
 *
 */
public class TestSetupUtil {

	static Session session = null;

	@AfterClass
	public static void afterTest() {
		tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() {
		setupEmbeddedCassandra();
	}

	protected static void setupEmbeddedCassandra(String... querys) {

		try {
			EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void executeScript(String... querys) {

		try {
			session = CassandraConnector.getSession();
			for (String query : querys) {
				session.execute(query);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void tearEmbeddedCassandraSetup() {
		try {
			session.close();
			EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}