package org.sunbird.common.mgr;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.cache.factory.JedisFactory;
import org.sunbird.graph.engine.router.GraphEngineManagers;

import redis.clients.jedis.Jedis;
public abstract class HealthCheckManager extends BaseManager{

	public abstract Response getAllServiceHealth() throws Exception;

	protected Map<String, Object> checkGraphHealth(String graphId){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", graphId + " graph");

		try{
			Request createReq = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "upsertRootNode");
			Response res = getResponse(createReq);
			if (checkError(res)) {
	    		check.put("healthy", false);
	    		check.put("err", ""); // error code, if any
	            check.put("errmsg", getErrorMessage(res)); // default English error message 				
			} else {
				check.put("healthy", true);
			}

		}catch (Exception e) {
			e.printStackTrace();
    		check.put("healthy", false);
    		check.put("err", ""); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
        }
		
		return check;
	}
	
	protected static Map<String, Object> checkRedisHealth(){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "redis cache");
        try {
        	Jedis jedis = JedisFactory.getRedisConncetion();
            jedis.close();
    		check.put("healthy", true);
        } catch (Exception e) {
        	e.printStackTrace();
    		check.put("healthy", false);
    		check.put("err", "503"); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
        }
		return check;
	}

}
