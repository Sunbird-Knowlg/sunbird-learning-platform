package org.ekstep.language.util;

import java.util.Iterator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

public class CustomTransactionEventHandler implements TransactionEventHandler<Void>{

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

}
