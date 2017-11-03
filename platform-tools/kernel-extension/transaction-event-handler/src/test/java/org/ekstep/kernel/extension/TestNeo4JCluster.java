package org.ekstep.kernel.extension;

import java.io.File;

import org.neo4j.cluster.ClusterSettings;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.kernel.ha.HaSettings;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;

public class TestNeo4JClusterMaster {
	protected final static String DB_LOCATION = "target/graph-master";
	protected final static String SERVER_ID = "1";
	HighlyAvailableGraphDatabase graphDb;
	
	
	public static void main(String[] args) throws InterruptedException {
		new TestNeo4JClusterMaster();
	}

	@SuppressWarnings("unchecked")
	public TestNeo4JClusterMaster() throws InterruptedException {
		GraphDatabaseBuilder builder = new HighlyAvailableGraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(DB_LOCATION));

		builder.setConfig(ClusterSettings.server_id, SERVER_ID);
		builder.setConfig(HaSettings.ha_server, "localhost:4001");
		builder.setConfig(HaSettings.slave_only, "false");
		builder.setConfig(ClusterSettings.cluster_server, "localhost:3001");
		builder.setConfig(ClusterSettings.initial_hosts, "localhost:3001");

		graphDb = (HighlyAvailableGraphDatabase) builder.newGraphDatabase();
		EkStepTransactionEventHandler handler = new EkStepTransactionEventHandler(graphDb);
		graphDb.registerTransactionEventHandler(handler);
		
		
		Label name = DynamicLabel.label("domain");
		
		boolean a = true;
		
		while (a) {
			try (Transaction tx = graphDb.beginTx()) {
				Node node = graphDb.createNode(name);
				String uniqueId = "id_" + Math.random();
				node.setProperty("IL_UNIQUE_ID", uniqueId);
				node.setProperty("name", "Name_" + uniqueId);
				tx.success();
				System.out.println("Added node");
				a = false;
			}
			Thread.sleep(10000);
		}
		graphDb.shutdown();
		
	}

}
