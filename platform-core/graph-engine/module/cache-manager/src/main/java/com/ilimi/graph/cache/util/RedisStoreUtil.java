package com.ilimi.graph.cache.util;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.common.mgr.CachePropertyConfiguration;

import redis.clients.jedis.Jedis;


public class RedisStoreUtil {


	public static void saveNodeProperty(String graphId, String objectId, String nodeProperty, String propValue) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = RedisKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			jedis.set(redisKey, propValue);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static String getNodeProperty(String graphId, String objectId, String nodeProperty) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = RedisKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			String value = jedis.get(redisKey);
			return value;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static Map<String, Object> getDataNode(String graphId, String objectId) {

		Jedis jedis = getRedisConncetion();
		try {
			List<String> cachedProperties = CachePropertyConfiguration.getProperties("data");
			Map<String, Object> nodeMetadata = new HashMap<String, Object>();

			for(String property : cachedProperties){
				String redisKey = RedisKeyGenerator.getNodePropertyKey(graphId, objectId, property);
				nodeMetadata.put(property, jedis.get(redisKey));
			}
			return nodeMetadata;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}
	
	public static Map<String, Object> getDefinitionNode(String graphId, String objecttype) {

		Jedis jedis = getRedisConncetion();
		try {
			List<String> cachedProperties = CachePropertyConfiguration.getProperties("definition");
			Map<String, Object> nodeMetadata = new HashMap<String, Object>();

			for(String property : cachedProperties){
				String redisKey = RedisKeyGenerator.getNodePropertyKey(graphId, objecttype, property);
				nodeMetadata.put(property, jedis.get(redisKey));
			}
			return nodeMetadata;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}
	
	public static void saveNode(String graphId, String objecttype, Map<String, Object> metadata){
		Jedis jedis = getRedisConncetion();
		try {
			for(Entry<String, Object> entry : metadata.entrySet()){
				String propertyName = entry.getKey();
				String propertyValue = entry.getValue().toString();
				
				String redisKey = RedisKeyGenerator.getNodePropertyKey(graphId, objecttype, propertyName);
				jedis.set(redisKey, propertyValue);
			}
			
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}
	
}
