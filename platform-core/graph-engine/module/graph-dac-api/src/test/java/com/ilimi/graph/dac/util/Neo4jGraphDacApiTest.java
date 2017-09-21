package com.ilimi.graph.dac.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import com.ilimi.common.dto.Request;

public class Neo4jGraphDacApiTest {

	public static final String graphId = "testGraph";
	public static final String testId = "xyz";
	public static GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
	String IL_UNIQUE_ID = "JAVA001";
	String startNodeId = "JAVA001";
	String endNodeId = "SCALA001";
	String relationType = "ASSOCIATED_TO";

	@BeforeClass
	public static void init() throws Exception {
		TestGraph.createNode(graphDb);
	}

	@Test
	public void checkIfGraphExists() {
		Boolean result = Neo4jGraphFactory.graphExists(graphId);
		assertEquals(true, result);
	}

	@Test
	public void BatchInsertorTest() {
		try {
			Neo4jGraphFactory.deleteGraph(testId);
			BatchInserter result = Neo4jGraphFactory.getBatchInserter(testId);
			assertEquals(false, result.getStoreDir().isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createConstraintsTest() {
		try (Transaction tx = graphDb.beginTx()) {
			Neo4jGraphFactory.createConstraints(graphDb);
			ResourceIterator<Node> res = graphDb.getAllNodes().iterator();
			assertEquals(true, res.hasNext());
			assertEquals(true, res.next().hasProperty("IL_SYS_NODE_TYPE"));
			tx.close();
		}
	}

	@Test
	public void getGraphDbTest() {
		Request request = new Request();
		request.setId(graphId);
		GraphDatabaseService graph = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			ResourceIterator<Node> res = graph.getAllNodes().iterator();
			assertEquals(true, res.hasNext());
			assertEquals(true, res.next().hasProperty("IL_UNIQUE_ID"));
			tx.close();
		}
	}

	@Test
	public void getNodeByUniqueIdTest() {
		try (Transaction tx = graphDb.beginTx()) {
			Node result = Neo4jGraphUtil.getNodeByUniqueId(graphDb, IL_UNIQUE_ID);
			String uniqueNodeIdName = (String) result.getProperty("Title");
			assertEquals("Learn Java", uniqueNodeIdName);
			tx.success();
		}
	}

	@Test
	public void getRelationsTest() {
		try (Transaction tx = graphDb.beginTx()) {
			Relationship result = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
			String startNodeName = (String) result.getStartNode().getProperty("Title");
			assertEquals("Learn Java", startNodeName);
			tx.success();
		}
	}

	@AfterClass
	public static void close() throws IOException, InterruptedException {
		if (graphDb != null) {
			graphDb.shutdown();
			Neo4jGraphFactory.deleteGraph(graphId);
		}
	}
}
