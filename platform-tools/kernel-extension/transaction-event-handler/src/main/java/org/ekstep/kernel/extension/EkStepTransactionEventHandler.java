package org.ekstep.kernel.extension;

import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.util.ProcessTransactionData;


@SuppressWarnings("rawtypes")
public class EkStepTransactionEventHandler implements TransactionEventHandler {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	public static HighlyAvailableGraphDatabase db;

	public EkStepTransactionEventHandler(HighlyAvailableGraphDatabase graphDatabaseService) {
		db = graphDatabaseService;
	}

	@Override
	public Void beforeCommit(TransactionData transactionData) throws Exception {
		try {
			LOGGER.log("Checking if the Current Instance is Master...." , db.isMaster());
			if (db.isMaster()) {
				LOGGER.log("Processing the Transaction as I am the Master." , db.role());
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
		LOGGER.log("After Commit Executed.");
	}

	@Override
	public void afterRollback(TransactionData transactionData, Object o) {
		LOGGER.log("After Rollback Executed.");
	}
}
