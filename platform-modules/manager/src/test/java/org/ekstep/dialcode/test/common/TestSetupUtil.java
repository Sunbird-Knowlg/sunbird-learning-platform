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

	private static String cassandraScript_1="CREATE KEYSPACE IF NOT EXISTS dialcode_store_test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};";
	private static String cassandraScript_2="CREATE TABLE IF NOT EXISTS dialcode_store_test.dial_code_test (identifier text,dialcode_index double,publisher text,channel text,batchCode text,metadata text,status text,generated_on text,published_on text, primary key(identifier));";
	
	@AfterClass
	public static void afterTest() {
		tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() {
		setupEmbeddedCassandra(cassandraScript_1, cassandraScript_2);
	}
	
	private static void setupEmbeddedCassandra(String...querys) {
		try {
			 try {
				 System.out.println("Starting Cassandra");
				 EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
				 System.out.println("Cassandra Started");
				 
				 System.out.println("Creating Schema");
				 Session session =  CassandraConnector.getSession();
				 for(String query:querys){
					 session.execute(query);
				 }
				 System.out.println("Schema created");
				 
				
			} catch (TTransportException | IOException e) {
				System.out.println("********************");
				e.printStackTrace();
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void tearEmbeddedCassandraSetup() {
		try {
			EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}