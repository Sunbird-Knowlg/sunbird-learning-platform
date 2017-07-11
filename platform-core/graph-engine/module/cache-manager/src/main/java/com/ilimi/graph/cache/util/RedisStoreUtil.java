package com.ilimi.graph.cache.util;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;

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

}
