package org.sunbird.graph.dac.util;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

public class TestGraph {
	
	public static final Label NODE_LABEL = Label.label("NODE");
	
	public static void createNode(GraphDatabaseService graphDb) {
		try (Transaction tx = graphDb.beginTx()) {
			Node javaNode = graphDb.createNode(NODE_LABEL);
			Node scalaNode = graphDb.createNode(NODE_LABEL);
			Node graphNode = graphDb.createNode(NODE_LABEL);
			Node sqlNode = graphDb.createNode(NODE_LABEL);

			javaNode.setProperty("IL_FUNC_OBJ_TYPE","Content");
			javaNode.setProperty("IL_SYS_NODE_TYPE","DEFINITION_NODE");
			javaNode.setProperty("IL_UNIQUE_ID", "JAVA001");
			javaNode.setProperty("Title", "Learn Java");
			javaNode.setProperty("NoOfChapters", "25");
			javaNode.setProperty("Status", "Completed");
		

			scalaNode.setProperty("IL_FUNC_OBJ_TYPE","Content");
			scalaNode.setProperty("IL_SYS_NODE_TYPE","DEFINITION_NODE");
			scalaNode.setProperty("IL_UNIQUE_ID", "SCALA001");
			scalaNode.setProperty("Title", "Learn Scala");
			scalaNode.setProperty("NoOfChapters", "20");
			scalaNode.setProperty("Status", "Completed");

			graphNode.setProperty("IL_FUNC_OBJ_TYPE","Content");
			graphNode.setProperty("IL_SYS_NODE_TYPE","DEFINITION_NODE");
			graphNode.setProperty("IL_UNIQUE_ID", "NEO4J001");
			graphNode.setProperty("Title", "Learn neo4j");
			graphNode.setProperty("NoOfChapters", "20");
			graphNode.setProperty("Status", "Completed");

			sqlNode.setProperty("IL_FUNC_OBJ_TYPE","Content");
			sqlNode.setProperty("IL_SYS_NODE_TYPE","DEFINITION_NODE");
			sqlNode.setProperty("IL_UNIQUE_ID", "SQL001");
			sqlNode.setProperty("Title", "Learn SQL");
			sqlNode.setProperty("NoOfChapters", "20");
			sqlNode.setProperty("Status", "Completed");

			Relationship relationship = javaNode.createRelationshipTo(scalaNode, RelationEnums.ASSOCIATED_TO);
			relationship.setProperty("Title","JVM_LANGUAGES");
			relationship.setProperty("Id", "1234");
			relationship.setProperty("OOPS", "YES");
			relationship.setProperty("FP", "YES");
			tx.success();
		}
	}
}