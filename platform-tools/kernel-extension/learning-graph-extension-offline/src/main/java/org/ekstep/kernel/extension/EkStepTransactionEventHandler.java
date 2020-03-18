package org.ekstep.kernel.extension;

import org.ekstep.telemetry.logger.TelemetryManager;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;


@SuppressWarnings("rawtypes")
public class EkStepTransactionEventHandler implements TransactionEventHandler {

    public static GraphDatabaseService db;

    public EkStepTransactionEventHandler(GraphDatabaseService graphDatabaseService) {
        db =  graphDatabaseService;
    }

    @Override
    public Void beforeCommit(TransactionData transactionData) throws Exception {
        try {
            ProcessTransactionData processTransactionData = new ProcessTransactionData(
                    "domain", db);
            processTransactionData.processTxnData(transactionData);
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    @Override
    public void afterCommit(TransactionData transactionData, Object o) {
        TelemetryManager.log("After Commit Executed.");
    }

    @Override
    public void afterRollback(TransactionData transactionData, Object o) {
        TelemetryManager.log("After Rollback Executed.");
    }
}