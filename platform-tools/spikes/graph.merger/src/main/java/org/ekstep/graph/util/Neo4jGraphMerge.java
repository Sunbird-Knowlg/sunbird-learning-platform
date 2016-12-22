package org.ekstep.graph.util;

import java.io.File;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.Schema;

import gnu.trove.map.hash.TLongLongHashMap;
import org.ekstep.graph.enums.SystemProperties;

public class Neo4jGraphMerge {

	private static String graphDbPath ="/data/graphDB";

	static final int BATCH = 100;

	public static void main(String arg[]){
		try {
			Neo4jGraphMerge graphMerge = new Neo4jGraphMerge();
			long startTime = System.currentTimeMillis();
			if(arg.length>=2){
				for(int i=1;i<arg.length;i++)
					graphMerge.migrate(arg[i], arg[0]);				
			}else{
				System.out.println("arguments should be more than one like destination_id followed by source_ids ex. Master, en, domain, hi ");
			}
			//graphMerge.migrate("domain", "master");
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
		    System.out.println((elapsedTime/1000)+" - seconds it took");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void merge(List<String> source_ids, String destination_id) throws Exception{
		for(String source_id : source_ids){
				migrate(source_id, destination_id);
		}
	}
	
    public void migrate(String source_id, String destination_id) {

        Transaction txSource = null;
        Transaction txDest = null;
        GraphDatabaseService dbSrc = null; 
        GraphDatabaseService dbDest = null;
    	try {

            dbSrc = getDb(graphDbPath + File.separator + source_id);
            dbDest = getDb(graphDbPath + File.separator + destination_id);
            //create indexes and constraints
            createConstraints(dbDest, Label.label(source_id));
            //clean existing nodes having same labels in destination(Master) db
            clean(dbDest, Label.label(source_id));
            logDBstatus(dbSrc, source_id);

        	txSource = dbSrc.beginTx();
        	TLongLongHashMap nodeIdMap = new TLongLongHashMap();
            int batchCount = 0;
            int totalNode = 0;
            txDest = dbDest.beginTx();
                        
            for (Node srcNode : dbSrc.getAllNodes()) {
            	
            	if(batchCount==BATCH){
            		batchCount = 0;
            		txDest.success();
                    txDest.close();
            		txDest = dbDest.beginTx();
            	}
                
                Node destNode = dbDest.createNode();
                nodeIdMap.put(srcNode.getId(), destNode.getId());

                //add labels
                destNode.addLabel(Label.label(source_id));
                 
                //add properties
                for (String srcKey : srcNode.getPropertyKeys())
                    destNode.setProperty(srcKey, srcNode.getProperty(srcKey));

                batchCount++;
                totalNode++;
            }
            
            txDest.success();
            txDest.close();
            System.out.println(totalNode + " - nodes were copied from "+source_id+" to master");
            txDest = dbDest.beginTx();
            int totalRel = 0;
            for (Relationship srcRel : dbSrc.getAllRelationships()) {
                
            	if(batchCount==BATCH){
            		batchCount = 0;
            		txDest.success();
                    txDest.close();
            		txDest = dbDest.beginTx();		
            	}

            	Node startNode = dbDest.getNodeById(nodeIdMap.get(srcRel.getStartNode().getId()));
                Node endNode = dbDest.getNodeById(nodeIdMap.get(srcRel.getEndNode().getId()));

                Relationship destRel = startNode.createRelationshipTo(endNode, srcRel.getType());
                for (String srcKey : srcRel.getPropertyKeys())
                    destRel.setProperty(srcKey, srcRel.getProperty(srcKey));
                
                batchCount++;
                totalRel++;
            }

            txDest.success();
            txDest.close();
            txSource.success();
            txSource.close();

            System.out.println(totalRel+ " - relationships were copied from "+source_id+" to master");
        }catch(Exception e){
        	System.out.println("Merge Error " + e.getMessage());
        	e.printStackTrace();
        }finally{
            if(txDest !=null)
            	txDest.close();
            
            if(txSource != null)
            	txSource.close();
            
            if(dbDest!=null) logDBstatus(dbDest, destination_id);

            System.out.println(source_id+ " graph migration is success");
            if(dbDest!=null) dbDest.shutdown();
            if(dbSrc!=null) dbSrc.shutdown();

        }

    }
    
    private GraphDatabaseService getDb(String dbPath){
		GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
		GraphDatabaseService db = dbFactory.newEmbeddedDatabase(new File(dbPath));
		return db;
    }
    
    private void createConstraints(GraphDatabaseService graphDb, Label label) {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Schema schema = graphDb.schema();
            schema.indexFor(label).on(SystemProperties.IL_SYS_NODE_TYPE.name()).create();
            schema.indexFor(label).on(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).create();
            schema.constraintFor(label).assertPropertyIsUnique(SystemProperties.IL_UNIQUE_ID.name()).create();
            tx.success();
        } catch (Exception e) {
        	e.printStackTrace();
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
    }
    
    private void clean(GraphDatabaseService graphDb, Label label) {
    	Transaction tx =null;
    	int batchCount = 0;
    	int cleanCount = 0;
    	try {
    		tx = graphDb.beginTx();
    		ResourceIterator<Node> nodes =graphDb.findNodes(label);
    		
			while(nodes.hasNext()) {
				
				if(batchCount == BATCH){
					tx.close();
					batchCount =0;
					tx = graphDb.beginTx();
				}
				
				Node node = nodes.next();
				Iterable<Relationship> rels = node.getRelationships();
				if (null != rels) {
					for (Relationship rel : rels) {
						rel.delete();
					}
				}
				node.delete();
				tx.success();
				batchCount++;
				cleanCount++;
			}
			
		}finally {
            if (null != tx)
                tx.close();
            System.out.println(cleanCount+ " - existing nodes were deleted with its relationships in master");  
        }
    }
    
    private void logDBstatus(GraphDatabaseService graphDb, String id){
        String nodeCountQuery = "match (n) return count(n)";
        Result res1= graphDb.execute(nodeCountQuery);
        System.out.println(nodeCountQuery+ "in "+id+" - Result ->"+ res1.resultAsString());
                
        String relationCountQuery = "match ()-[r]->() return count(r)";
        Result res2= graphDb.execute(relationCountQuery);
        System.out.println(relationCountQuery+ "in "+id+" - Result ->"+ res2.resultAsString());
    }
    
}
