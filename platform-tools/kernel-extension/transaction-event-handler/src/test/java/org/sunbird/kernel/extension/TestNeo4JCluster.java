package org.sunbird.kernel.extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cluster.ClusterSettings;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.kernel.ha.HaSettings;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;

public class TestNeo4JCluster {
	protected final static String DB_LOCATION = "target/graph-master";
	protected final static String SERVER_ID = "1";
	private static HighlyAvailableGraphDatabase graphDb;
	private Label name = Label.label("domain");
	private static Set<String> tmp = new HashSet<String>();
	private static String ch="";

	@BeforeClass
	public static void beforeTest() throws InterruptedException, FileNotFoundException {
		GraphDatabaseBuilder builder = new HighlyAvailableGraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(DB_LOCATION));
		builder.setConfig(ClusterSettings.server_id, SERVER_ID);
		builder.setConfig(HaSettings.ha_server, "localhost:4001");
		builder.setConfig(HaSettings.slave_only, "false");
		builder.setConfig(ClusterSettings.cluster_server, "localhost:3001");
		builder.setConfig(ClusterSettings.initial_hosts, "localhost:3001");
		graphDb = (HighlyAvailableGraphDatabase) builder.newGraphDatabase();
		deleteFileContents();
	}
	
	@org.junit.AfterClass
	public static void afterTest() throws FileNotFoundException {
		deleteFileContents();
	}
	
	@Test
	public void createNodeTest() {	
		try {
			try(Transaction tx = graphDb.beginTx()){
				Node node = graphDb.createNode(name);
				String uniqueId = "id_" + Math.random();
				node.setProperty("IL_UNIQUE_ID", uniqueId);
				node.setProperty("name", "Name_" + uniqueId);
				tx.success();
				File file = new File("/data/logs/test_graph_event_neo4j.log");
				Assert.assertTrue(file.exists());
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void addProperty() {
		try {
			String uniqueId = createNode(graphDb);
			try(	Transaction tx1 = graphDb.beginTx()){
				Node nodeData = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId);
				nodeData.setProperty("channel", "in.ekstep");
				nodeData.setProperty("IL_SYS_NODE_TYPE", "DATA_NODE");
				tx1.success();
				File file = new File("/data/logs/test_graph_event_neo4j.log");
				Assert.assertTrue(file.exists());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void updateProperty() {
		try {
			String uniqueId = createNode(graphDb);
			try(	Transaction tx1 = graphDb.beginTx()){
				Node nodeData = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId);
				nodeData.setProperty("description", "update description property");
				tx1.success();
				File file = new File("/data/logs/test_graph_event_neo4j.log");
				Assert.assertTrue(file.exists());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void removeProperty() {
		try {
			String uniqueId = createNode(graphDb);
			try(	Transaction tx1 = graphDb.beginTx()){
				Node nodeData = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId);
				nodeData.removeProperty("channel");
				tx1.success();
				File file = new File("/data/logs/test_graph_event_neo4j.log");
				Assert.assertTrue(file.exists());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void deleteNode() {
		try {
			String uniqueId = createNode(graphDb);
			try(	Transaction tx1 = graphDb.beginTx()){
				Node nodeData = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId);
				nodeData.delete();
				tx1.success();
				File file = new File("/data/logs/test_graph_event_neo4j.log");
				Assert.assertTrue(file.exists());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void addRelation() {
		try {
				String uniqueId1 = createNode(graphDb);
				String uniqueId2 = createNode(graphDb);
				try(	Transaction tx1 = graphDb.beginTx()){
					Node nodeData1 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId1);
					Node nodeData2 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId2);
					nodeData2.createRelationshipTo(nodeData1, RelationEnums.associatedTo);
					tx1.success();
					File file = new File("/data/logs/test_graph_event_neo4j.log");
					Assert.assertTrue(file.exists());
				}		
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	

	@Test
	public void addRelationProperty() {
		try {
				String uniqueId1 = createNode(graphDb);
				String uniqueId2 = createNode(graphDb);
				try(	Transaction tx1 = graphDb.beginTx()){
					Node nodeData1 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId1);
					Node nodeData2 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId2);
					nodeData2.createRelationshipTo(nodeData1, RelationEnums.isParentOf);
					tx1.success();
				}	
				try(	Transaction tx2 = graphDb.beginTx()){
					Node nodeData2 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId2);
					Relationship res = nodeData2.getSingleRelationship(RelationEnums.isParentOf, Direction.OUTGOING);
					res.setProperty("description", "isParentOf");
					tx2.success();
				}
			File file = new File("/data/logs/test_graph_event_neo4j.log");
			Assert.assertTrue(file.exists());
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	@Test
	public void removeRelationProperty() {
		try {
				String uniqueId1 = createNode(graphDb);
				String uniqueId2 = createNode(graphDb);
				try(	Transaction tx1 = graphDb.beginTx()){
					Node nodeData1 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId1);
					Node nodeData2 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId2);
					Relationship res = nodeData2.createRelationshipTo(nodeData1, RelationEnums.hasSequenceMember);
					res.setProperty("description", "hasSequenceMember");
					res.setProperty("relatioName","has sequence member");
					tx1.success();
				}		
				try(Transaction tx = graphDb.beginTx()){
					Node nodeData2 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId2);
					Relationship res = nodeData2.getSingleRelationship(RelationEnums.hasSequenceMember, Direction.OUTGOING);
					res.removeProperty("description");
					tx.success();
				}
			File file = new File("/data/logs/test_graph_event_neo4j.log");
			Assert.assertTrue(file.exists());
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	@Test
	public void removedRelation() {
		try {
				String uniqueId1 = createNode(graphDb);
				String uniqueId2 = createNode(graphDb);
				try(	Transaction tx = graphDb.beginTx()){
					Node nodeData1 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId1);
					Node nodeData2 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId2);
					nodeData2.createRelationshipTo(nodeData1, RelationEnums.associatedTo);
					tx.success();
				}
				try(Transaction tx = graphDb.beginTx()){
					Node nodeData2 = graphDb.findNode(name, "IL_UNIQUE_ID", uniqueId2);
					Relationship res = nodeData2.getSingleRelationship(RelationEnums.associatedTo, Direction.OUTGOING);
					res.delete();
					tx.success();
				}
			File file = new File("/data/logs/test_graph_event_neo4j.log");
			Assert.assertTrue(file.exists());
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	@SuppressWarnings("resource")
	public static String readFromFile() throws Exception {
		BufferedReader in = new BufferedReader(new FileReader("/data/logs/test_graph_event_neo4j.log"));
	    do {
	        ch = in.readLine();
	        tmp.add(ch);
	    } while (ch != null);
	    for(String s : tmp) {
	    		if(null != s)
	    			ch = s;
	    }
	    return ch;
	}
	
	public static void deleteFileContents() throws FileNotFoundException {
		File file = new File("/data/logs/test_graph_event_neo4j.log");
		if(file.exists())
			file.delete();
	}
	
	public String createNode(HighlyAvailableGraphDatabase graphDb2) {	
		String uniqueId = "";
		try {
			try(Transaction tx = graphDb.beginTx()){
				Node node = graphDb.createNode(name);
				uniqueId = "id_" + Math.random();
				node.setProperty("IL_UNIQUE_ID", uniqueId);
				node.setProperty("name", "Name_" + uniqueId);
				node.setProperty("IL_SYS_NODE_TYPE", "DATA_NODE");
				node.setProperty("description", "test description");
				tx.success();	
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return uniqueId;
	}
}
