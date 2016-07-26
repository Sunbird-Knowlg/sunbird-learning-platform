package org.ekstep.common.util;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.cache.factory.JedisFactory;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import redis.clients.jedis.Jedis;

public class HealthCheckUtil {
	
	public static Map<String, Object> checkMongoDB(){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "MongoDB");
		
		try{
			MongoClient mongoClient = new MongoClient();
			MongoDatabase database = mongoClient.getDatabase(MongoPropertiesUtils.getProperty("mongo.dbName"));
			Document serverStatus = database.runCommand(new Document("serverStatus", 1));
			Map connections = (Map) serverStatus.get("connections");
			Integer current = (Integer) connections.get("current");
			if( current >= 1){
				check.put("healthy", true);				
			}else{
				check.put("healthy", false);
	    		check.put("err", "503"); // error code, if any
	            check.put("errmsg", "No Mongo Active current conection found, problem with MongoDB connection pooling "); // default English error message 
			}

		}catch(Exception e){
    		check.put("healthy", false);
    		check.put("err", "503"); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
		}

		return check;
	}
	
	public static Map<String, Object> checkRedis(){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "redis cache");
        try {
        	Jedis jedis = JedisFactory.getRedisConncetion();
            jedis.close();
    		check.put("healthy", true);
        } catch (Exception e) {
    		check.put("healthy", false);
    		check.put("err", "503"); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
        }
		return check;
	}
}
