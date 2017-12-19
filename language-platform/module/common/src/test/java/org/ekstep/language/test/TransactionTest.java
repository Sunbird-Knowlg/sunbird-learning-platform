package org.ekstep.language.test;

import org.junit.Ignore;
@Ignore
public class TransactionTest {
	/*
	
	private Neo4JTransactionEventHandler transEventHandler = null;
	public static final Label NODE_LABEL = org.ekstep.graph.dac.enums.Label.NODE;
	
	@SuppressWarnings("unused")
	@Test
	public void TransactionTestTest() {
	GraphDatabaseService hiGraphDb = null;// Neo4jGraphFactory.getGraphDb("domain");
	GraphDatabaseService kaGraphDb = null;// Neo4jGraphFactory.getGraphDb("testOne");
	
	transEventHandler = new Neo4JTransactionEventHandler("test", hiGraphDb);
	//kaGraphDb.registerTransactionEventHandler(transEventHandler);
	
	
	Transaction tx = hiGraphDb.beginTx();
	try {
	Node neo4jNodeOther = hiGraphDb.createNode(NODE_LABEL);
	neo4jNodeOther.setProperty(SystemProperties.IL_UNIQUE_ID.name(), "50");
	neo4jNodeOther.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
	neo4jNodeOther.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Word");
	// tx = graphDb.beginTx();
	Node neo4jNode = hiGraphDb.getNodeById(140701);
	neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
	neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "synset");
	tx.success();
	} finally {
	tx.success();
	}
	
	Transaction kaTx = kaGraphDb.beginTx();
	try {
	Node neo4jNodeOther = kaGraphDb.createNode(NODE_LABEL);
	neo4jNodeOther.setProperty(SystemProperties.IL_UNIQUE_ID.name(), "40");
	neo4jNodeOther.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
	neo4jNodeOther.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Word");
	neo4jNodeOther.setProperty("lemma", "Hi");
	// tx = graphDb.beginTx();
	Node neo4jNode = kaGraphDb.getNodeById(140701);
	neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
	neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "synset");
	//kaTx.success();
	} finally {
	//kaTx.finish();
	}
	
	*//**
		 * prints: > Committing transaction > Committed transaction
		 **/
	/*
	}
	
	*/}
