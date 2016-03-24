package com.ilimi.graph.dac.util;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

public class Neo4JTransactionEventHandler implements TransactionEventHandler<Void>{
	
	protected String graphId;
	protected GraphDatabaseService graphDb;

	public Neo4JTransactionEventHandler(String graphId, GraphDatabaseService graphDb) {
		this.graphId = graphId;
		this.graphDb = graphDb;
	}

	@Override
	public Void beforeCommit(TransactionData data) throws Exception {
	    try {
	        ProcessTransactionData processTransactionData = new ProcessTransactionData(graphId, graphDb);
            processTransactionData.processTxnData(data);
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	    }
		return null;
	}

	@Override
	public void afterCommit(TransactionData data, Void state) {
	}

	@Override
	public void afterRollback(TransactionData data, Void state) {
	}
	
}
