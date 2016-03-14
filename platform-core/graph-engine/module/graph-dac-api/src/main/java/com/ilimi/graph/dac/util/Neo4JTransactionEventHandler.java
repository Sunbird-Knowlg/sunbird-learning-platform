package com.ilimi.graph.dac.util;

import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.*;

public class Neo4JTransactionEventHandler {
	
	public static void registerTransactionEventForGraph(GraphDatabaseService graphDB) {
    	GraphDatabaseService database = graphDB;
    	System.out.println("Registering Transaction Event!");

        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                System.out.println("Committing transaction");
                return null;
            }

            @Override
            public void afterCommit(TransactionData data, Void state) {
                System.out.println("Committed transaction");
                processTxnData(data);
            }

            @Override
            public void afterRollback(TransactionData data, Void state) {
                System.out.println("Transaction rolled back");
            }
        });
        System.out.println("Transaction Event Registered!");
    }
	
	public static void processTxnData (TransactionData data) {
		System.out.println("Txn Data : " + data);
		try
		{
		ObjectMapper mapper = new ObjectMapper();

		//Object to JSON in String
		String jsonInString = mapper.writeValueAsString(data);
		
		
		    String filename= "/data/tmp/temp/TrxEventHndlr.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    fw.write("add a line\n");//appends the string to the file
		    fw.write("------------------------------------------------------------------------------------------------");
		    fw.write(jsonInString);
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}
}
