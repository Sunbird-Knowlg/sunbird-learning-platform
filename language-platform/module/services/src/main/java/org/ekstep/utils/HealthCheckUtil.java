package org.ekstep.utils;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.language.util.WordCacheUtil;
import org.neo4j.graphdb.GraphDatabaseService;

import com.ilimi.graph.dac.util.Neo4jGraphFactory;

import redis.clients.jedis.Jedis;

public class HealthCheckUtil {

	public static Map<String, Object> checkNeo4jGraph(String id){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", id + " graph");

		try{
			GraphDatabaseService db = Neo4jGraphFactory.getGraphDb(id);
			check.put("healthy", true);
		}catch (Exception e) {
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
