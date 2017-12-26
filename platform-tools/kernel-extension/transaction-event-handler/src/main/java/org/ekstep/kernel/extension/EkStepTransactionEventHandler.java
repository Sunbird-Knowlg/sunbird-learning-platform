package org.ekstep.kernel.extension;

import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.dac.util.ProcessTransactionData;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;


@SuppressWarnings("rawtypes")
public class EkStepTransactionEventHandler implements TransactionEventHandler {

	

	public static HighlyAvailableGraphDatabase db;

	public EkStepTransactionEventHandler(HighlyAvailableGraphDatabase graphDatabaseService) {
		db = graphDatabaseService;
	}

	@Override
	public Void beforeCommit(TransactionData transactionData) throws Exception {
		try {
			PlatformLogger.log("Checking if the Current Instance is Master...." , db.isMaster());
			if (db.isMaster()) {
				PlatformLogger.log("Processing the Transaction as I am the Master." , db.role());
				ProcessTransactionData processTransactionData = new ProcessTransactionData(
						"domain", db);
				processTransactionData.processTxnData(transactionData);
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	@Override
	public void afterCommit(TransactionData transactionData, Object o) {
		PlatformLogger.log("After Commit Executed.");
	}

	@Override
	public void afterRollback(TransactionData transactionData, Object o) {
		PlatformLogger.log("After Rollback Executed.");
	}
}
