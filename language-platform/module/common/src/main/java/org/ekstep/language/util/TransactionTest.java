package org.ekstep.language.util;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import com.ilimi.graph.dac.enums.AuditProperties;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;

public class TransactionTest {

	@SuppressWarnings("deprecation")
	@Test
	public void TransactionTest() {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb("hi");

		graphDb.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
			@Override
			public Void beforeCommit(TransactionData data) throws Exception {
				System.out.println("Committing transaction");
				return null;
			}

			@SuppressWarnings("unused")
			@Override
			public void afterCommit(TransactionData data, Void state) {
				Iterable<Node> nodeIterable = data.createdNodes();
				Iterator<Node> nodeIterator = nodeIterable.iterator();
				while (nodeIterator.hasNext()) {
					Node node = nodeIterator.next();
				}
				Iterable<PropertyEntry<Node>> nodeProperties = data.assignedNodeProperties();
				Iterator<PropertyEntry<Node>> nodePropertiesIterator = nodeProperties.iterator();
				while (nodePropertiesIterator.hasNext()) {
					PropertyEntry<Node> propertyNode = nodePropertiesIterator.next();
					Object propertyEntry = propertyNode.value();
				}
				System.out.println("Committed transaction");
			}

			@Override
			public void afterRollback(TransactionData data, Void state) {
				System.out.println("Transaction rolled back");
			}
		});

		Transaction tx = graphDb.beginTx();
		try {
			Node neo4jNodeOther = graphDb.createNode(NODE_LABEL);
			neo4jNodeOther.setProperty(SystemProperties.IL_UNIQUE_ID.name(), "12");
			neo4jNodeOther.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
			neo4jNodeOther.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Word");
			// tx = graphDb.beginTx();
			Node neo4jNode = graphDb.getNodeById(140701);
			neo4jNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "Data");
			neo4jNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "synset");
			tx.success();
		} finally {
			tx.finish();
		}

		/**
		 * prints: > Committing transaction > Committed transaction
		 **/
	}

}
