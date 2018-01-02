package org.ekstep.dialcode.test.common;

import java.io.IOException;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
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
		tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() throws Exception {
		//tearEmbeddedCassandraSetup();
	}
	
	
	@SuppressWarnings("unused")
	protected static void setupEmbeddedCassandra(String...querys ) {
		
		try {
			 EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
			 System.out.println("Embedded Cassandra Started..");
			 
			 System.out.println("Creating Schema..");
			 //Session session =  CassandraConnector.getSession();
			 
			 Session session = EmbeddedCassandraServerHelper.getSession();
			 
			 for(String query:querys){
				 System.out.println("query::::::::::::"+query);
				 session.execute(query);
				 System.out.println("Completed...");
			 }
			 System.out.println("Schema created..");
			
		} catch (TTransportException | IOException | ConfigurationException e) {
			System.out.println("Exception Occured while setting up Embedded Cassandra : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void tearEmbeddedCassandraSetup() {
		EmbeddedCassandraServerHelper.getCluster().close();
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
		System.out.println("Cassandra Cleaned!!!!!!!!!!!!!!!!!");
	}
	
}
