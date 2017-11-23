package com.ilimi.orchestrator.common;

import java.io.IOException;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.datastax.driver.core.Session;
import com.ilimi.cassandra.connector.util.CassandraConnector;

public class TestSetup {

	@AfterClass
	public static void afterTest() {
		tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() {
		setupEmbeddedCassandra();
	}
	
	private static void setupEmbeddedCassandra() {
		try {
			 try {
				 System.out.println("Starting Cassandra");
				 EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
				 System.out.println("Cassandra Started");
				 
				 System.out.println("Creating Schema");
				 Session session =  CassandraConnector.getSession();
				 String query = "CREATE KEYSPACE IF NOT EXISTS script_store WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}";
				 session.execute(query);
				 query = "CREATE TABLE IF NOT EXISTS script_store.script_data (name text, type text, reqmap text, PRIMARY KEY (name))";
				 session.execute(query);
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
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}
	
}
