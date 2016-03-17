package org.ekstep.language.util;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;

public class TransactionTest {

	private CustomTransactionEventHandler transEventHandler = new CustomTransactionEventHandler();
	
	@SuppressWarnings("deprecation")
	@Test
	public void TransactionTestTest() {
		GraphDatabaseService hiGraphDb = Neo4jGraphFactory.getGraphDb("hi");
		GraphDatabaseService kaGraphDb = Neo4jGraphFactory.getGraphDb("ka");

		hiGraphDb.registerTransactionEventHandler(transEventHandler);

		kaGraphDb.registerTransactionEventHandler(transEventHandler);

		
		Transaction tx = hiGraphDb.beginTx();
		try {
			Node neo4jNodeOther = hiGraphDb.createNode(NODE_LABEL);
			neo4jNodeOther.setProperty(SystemProperties.IL_UNIQUE_ID.name(), "19");
			neo4jNodeOther.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
			neo4jNodeOther.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Word");
			// tx = graphDb.beginTx();
			Node neo4jNode = hiGraphDb.getNodeById(140701);
			neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
			neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "synset");
			tx.success();
		} finally {
			tx.finish();
		}

		Transaction kaTx = kaGraphDb.beginTx();
		try {
			Node neo4jNodeOther = kaGraphDb.createNode(NODE_LABEL);
			neo4jNodeOther.setProperty(SystemProperties.IL_UNIQUE_ID.name(), "20");
			neo4jNodeOther.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
			neo4jNodeOther.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Word");
			// tx = graphDb.beginTx();
			/*Node neo4jNode = kaGraphDb.getNodeById(140701);
			neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
			neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "synset");*/
			kaTx.success();
		} finally {
			kaTx.finish();
		}
		
		/**
		 * prints: > Committing transaction > Committed transaction
		 **/
	}

}
