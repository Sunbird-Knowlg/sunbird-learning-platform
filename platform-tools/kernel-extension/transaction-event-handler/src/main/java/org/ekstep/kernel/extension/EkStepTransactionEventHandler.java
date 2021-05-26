package org.ekstep.kernel.extension;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListener;


@SuppressWarnings("rawtypes")
public class EkStepTransactionEventHandler implements TransactionEventListener {

	public static GraphDatabaseService db;

	public EkStepTransactionEventHandler(GraphDatabaseService graphDatabaseService) {
		db =  graphDatabaseService;
	}

	@Override
	public Object beforeCommit(TransactionData transactionData, Transaction transaction, GraphDatabaseService graphDatabaseService) throws Exception {
		try {

			System.out.println("beforeCommit: " + graphDatabaseService.databaseName());
			transactionData.createdNodes().forEach(node -> {
				System.out.println("Received transaction data in before commit " + node.getId() + " Properties" + node.getAllProperties());
			});
			
			
			/*ProcessTransactionData processTransactionData = new ProcessTransactionData(
					"domain", db);
			processTransactionData.processTxnData(transactionData);*/
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	@Override
	public void afterCommit(TransactionData transactionData, Object o, GraphDatabaseService graphDatabaseService) {
		System.out.println("afterCommit: " + graphDatabaseService.databaseName());
		System.out.println("After Commit Executed.");
	}

	@Override
	public void afterRollback(TransactionData transactionData, Object o, GraphDatabaseService graphDatabaseService) {
		System.out.println("After Rollback Executed.");
	}
}
