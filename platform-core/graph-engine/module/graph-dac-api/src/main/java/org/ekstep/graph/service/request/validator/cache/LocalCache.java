package org.ekstep.graph.service.request.validator.cache;

import java.util.HashMap;
import java.util.Map;

import com.ilimi.graph.cache.util.RedisKeyGenerator;

public class LocalCache {

	private static Map<String, String> cache = null;
	
	static{
		init();
	}
	
	public static void init(){
		cache = new HashMap<String, String>();
	}
	
	public static String get(String graphId, String objectType, String nodeProperty){
		return cache.get(RedisKeyGenerator.getNodePropertyKey(graphId, objectType, nodeProperty));
	}
	
	public static void set(String graphId, String objectType, String nodeProperty, String value){
		String key = RedisKeyGenerator.getNodePropertyKey(graphId, objectType, nodeProperty);
		cache.put(key, value);
	}
}
