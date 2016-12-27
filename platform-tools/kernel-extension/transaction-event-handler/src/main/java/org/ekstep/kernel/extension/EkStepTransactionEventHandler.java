package org.ekstep.kernel.extension;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.kernel.extension.common.TransactionEventHandlerParams;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;


@SuppressWarnings("rawtypes")
public class EkStepTransactionEventHandler implements TransactionEventHandler {

	private static Logger LOGGER = LogManager.getLogger(EkStepTransactionEventHandler.class.getName());

	public static HighlyAvailableGraphDatabase db;

	public EkStepTransactionEventHandler(HighlyAvailableGraphDatabase graphDatabaseService) {
		db = graphDatabaseService;
	}

	@Override
	public Void beforeCommit(TransactionData transactionData) throws Exception {
		try {
			LOGGER.info("Checking if the Current Instance is Master...." + db.isMaster());
			if (db.isMaster()) {
				LOGGER.info("Processing the Transaction as I am the Master." + db.role());
				ProcessTransactionData processTransactionData = new ProcessTransactionData(
						TransactionEventHandlerParams.domain.name(), db);
				processTransactionData.processTxnData(transactionData);
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	@Override
	public void afterCommit(TransactionData transactionData, Object o) {
		LOGGER.info("After Commit Executed.");
	}

	@Override
	public void afterRollback(TransactionData transactionData, Object o) {
		LOGGER.info("After Rollback Executed.");
	}
}
