package org.ekstep.utils;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.language.util.WordCacheUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;

import redis.clients.jedis.Jedis;

public class HealthCheckUtil {

	
	private static String getRootNodeId(String graphId, String id){
		String prefix = "";
		if (graphId.length() >= 2)
			prefix = graphId.substring(0, 2);
		else
			prefix = graphId;
		String identifier = prefix + "_" + id;

		return identifier;
	}
	
	private static Node getRootNode(GraphDatabaseService graphDb, String graphId){
        String rootNodeUniqueId = getRootNodeId(graphId, SystemNodeTypes.ROOT_NODE.name());
        Transaction tx = null;
        try {
	        tx = graphDb.beginTx();
	        org.neo4j.graphdb.Node neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, rootNodeUniqueId);
	        graphDb.findNode(NODE_LABEL, SystemProperties.IL_UNIQUE_ID.name(), rootNodeUniqueId);
	        tx.success();
	        if(neo4jNode == null)
	        	return null;
	        Node node = new Node(graphId, neo4jNode);
	        tx.close();
	        return node;
        } catch (Exception e) {
            if (null != tx) {
                tx.failure();
                tx.close();
            }
            throw e;
        }
	}
	
	private static void createRootNode(GraphDatabaseService graphDb, String graphId){
        String rootNodeUniqueId = getRootNodeId(graphId, SystemNodeTypes.ROOT_NODE.name());
        org.neo4j.graphdb.Node rootNode = null;
        Transaction tx = null;
        tx = graphDb.beginTx();
        try{
    		rootNode = graphDb.createNode(NODE_LABEL);
    		rootNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), rootNodeUniqueId);
    		rootNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.ROOT_NODE.name());
    		rootNode.setProperty("nodesCount", 0);
            rootNode.setProperty("relationsCount", 0);
            tx.success();
            tx.close();
        }catch(Exception e){
            if (null != tx) {
                tx.failure();
                tx.close();
            }        	
            throw e;
        }
	}
	
	public static Map<String, Object> checkNeo4jGraph(String id){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", id + " graph");

		try{
			GraphDatabaseService db = Neo4jGraphFactory.getGraphDb(id);
			Node rootNode = getRootNode(db, id);
			if(rootNode == null)
				createRootNode(db, id);
			check.put("healthy", true);
		}catch (Exception e) {
    		check.put("healthy", false);
    		check.put("err", ""); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
        }
		
		return check;
	}
	
	public static Map<String, Object> checkRedis(){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "redis cache");
        try {
        	WordCacheUtil cacheUtil = new WordCacheUtil();
            Jedis jedis = cacheUtil.getRedisConncetion();
            jedis.close();
    		check.put("healthy", true);
        } catch (Exception e) {
    		check.put("healthy", false);
    		check.put("err", "503"); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
        }
		return check;
	}
	
	public static Map<String, Object> checkMongoDB(){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "MongoDB");
		check.put("healthy", true);
		return check;
	}
}
