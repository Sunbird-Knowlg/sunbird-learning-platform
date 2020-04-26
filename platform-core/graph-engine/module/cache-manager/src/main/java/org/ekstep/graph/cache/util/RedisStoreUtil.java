package org.ekstep.graph.cache.util;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.cache.exception.GraphCacheErrorCodes;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.telemetry.logger.TelemetryManager;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ekstep.graph.cache.factory.JedisFactory.getRedisConncetion;
import static org.ekstep.graph.cache.factory.JedisFactory.returnConnection;

public class RedisStoreUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	public static void saveNodeProperty(String graphId, String objectId, String nodeProperty, String propValue) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			jedis.set(redisKey, propValue);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}


	public static void save(String key, String value, int ttl) {

		Jedis jedis = getRedisConncetion();
		try {
			jedis.set(key, value);
			if(ttl > 0)
				jedis.expire(key, ttl);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static String get(String key) {
		Jedis jedis = getRedisConncetion();
		try {
			return jedis.get(key);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static String getNodeProperty(String graphId, String objectId, String nodeProperty) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			String value = jedis.get(redisKey);
			return value;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void saveNodeProperties(String graphId, String objectId, Map<String, Object> metadata) {
		Jedis jedis = getRedisConncetion();
		try {
			for (Entry<String, Object> entry : metadata.entrySet()) {
				String propertyName = entry.getKey();
				String propertyValue = entry.getValue().toString();

				String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, propertyName);
				jedis.set(redisKey, propertyValue);
			}

		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteNodeProperties(String graphId, String objectId) {
		Jedis jedis = getRedisConncetion();
		try {

			String versionKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId,
					GraphDACParams.versionKey.name());
			String consumerId = CacheKeyGenerator.getNodePropertyKey(graphId, objectId,
					GraphDACParams.consumerId.name());
			jedis.del(versionKey, consumerId);

		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteAllNodeProperty(String graphId, String propertyName) {
		String delKeysPattern = CacheKeyGenerator.getAllNodePropertyKeysPattern(graphId, propertyName);
		deleteByPattern(delKeysPattern);

	}

	public static Double getNodePropertyIncVal(String graphId, String objectId, String nodeProperty) {

		Jedis jedis = getRedisConncetion();
		try {
			String redisKey = CacheKeyGenerator.getNodePropertyKey(graphId, objectId, nodeProperty);
			double inc = 1.0;
			double value = jedis.incrByFloat(redisKey, inc);
			return value;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	
	// TODO: always considering object as string. need to change this.
	public static void saveList(String key, List<Object> values) {
		Jedis jedis = getRedisConncetion();
		try {
			jedis.del(key);
			for (Object val : values) {
				jedis.sadd(key, (String) val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnConnection(jedis);
		}
	}

	public static void saveStringList(String key, List<String> values) {
		Jedis jedis = getRedisConncetion();
		try {
			jedis.del(key);
			for (String val : values) {
				jedis.sadd(key, val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnConnection(jedis);
		}
	}

	public static List<String> getStringList(String key) {
		Jedis jedis = getRedisConncetion();
		try {
			Set<String> set = jedis.smembers(key);
			List<String> list = new ArrayList<String>(set);
			return list;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static List<Object> getList(String key) {
		Jedis jedis = getRedisConncetion();
		try {
			 Set<String> set = jedis.smembers(key);
			 List<Object> list = new ArrayList<Object>(set);
			return list;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_GET_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	/**
	 * This Method Save Data to Redis Cache With ttl.
	 *
	 * @param identifier
	 * @param data
	 * @param ttl
	 */
	public static void saveData(String identifier, Map<String, Object> data, int ttl) {
		try {
			TelemetryManager.log("Saving Content Data To Redis Cache having identifier : " + identifier);
			save(identifier, mapper.writeValueAsString(data), ttl);
		} catch (Exception e) {
			TelemetryManager.error("Error while saving data to Redis for Identifier : " + identifier + " | Error is : ", e);
		}
	}

	/**
	 * This method delete the keys from Redis Cache
	 * @param keys
	 */
	public static void delete(String... keys) {
		Jedis jedis = getRedisConncetion();
		try {
			jedis.del(keys);
		} catch (Exception e) {
			TelemetryManager.error("Error while deleting data from Redis for Identifiers : " + Arrays.asList(keys) + " | Error is : ", e);
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_DELETE_PROPERTY_ERROR.name(), e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	public static void deleteByPattern(String pattern) {
		if(StringUtils.isNotBlank(pattern) && !StringUtils.equalsIgnoreCase(pattern, "*")){
			Jedis jedis = getRedisConncetion();
			try {
				Set<String> keys = jedis.keys(pattern);
				if (keys != null && keys.size() > 0) {
					List<String> keyList = new ArrayList<>(keys);
					jedis.del(keyList.toArray(new String[keyList.size()]));
				}
			} catch (Exception e) {
				throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), e.getMessage());
			} finally {
				returnConnection(jedis);
			}
		}
	}

}
