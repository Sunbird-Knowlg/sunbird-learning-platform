package com.ilimi.graph.dac.util;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

public class Neo4JTransactionEventHandler implements TransactionEventHandler<Void>{
	
	protected String graphId;
	protected String userId;
	protected String requestId;
	protected GraphDatabaseService graphDb;

	public Neo4JTransactionEventHandler(String graphId, String userId, String requestId, GraphDatabaseService graphDb) {
		this.graphId = graphId;
		this.graphDb = graphDb;
		this.userId = (userId==null)?StringUtils.EMPTY:userId;
		this.requestId = (requestId==null)?StringUtils.EMPTY:requestId;
	}

	@Override
	public Void beforeCommit(TransactionData data) throws Exception {
	    try {
	        ProcessTransactionData processTransactionData = new ProcessTransactionData(graphId, userId, requestId, graphDb);
            processTransactionData.processTxnData(data);
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	    }
		return null;
	}

	@Override
	public void afterCommit(TransactionData data, Void state) {
		graphDb.unregisterTransactionEventHandler(this);
	}

	@Override
	public void afterRollback(TransactionData data, Void state) {
		graphDb.unregisterTransactionEventHandler(this);
	}
	
}
