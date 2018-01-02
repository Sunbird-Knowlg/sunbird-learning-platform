package org.ekstep.dialcode.test.common;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.datastax.driver.core.Session;
import org.ekstep.cassandra.connector.util.CassandraConnector;

/**
 * @author gauraw
 *
 */
public class TestSetupUtil{

	@AfterClass
	public static void afterTest() throws Exception {
		//tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() throws Exception {
		//tearEmbeddedCassandraSetup();
	}
	
	
	@SuppressWarnings("unused")
	protected static void setupEmbeddedCassandra(String...querys ) {
		 Session session =  CassandraConnector.getSession();
		 for(String query:querys){
			 session.execute(query);
		 }
		/*try {
			 	EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
				 Session session =  CassandraConnector.getSession();
		 		for(String query:querys){
			 		session.execute(query);
		 		}
		} catch (TTransportException | IOException | ConfigurationException e) {
			System.out.println("Exception Occured while setting up Embedded Cassandra : "+e.getMessage());
			e.printStackTrace();
		}*/
	}
	
	private static void tearEmbeddedCassandraSetup() {
		EmbeddedCassandraServerHelper.getCluster().close();
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}
	
}