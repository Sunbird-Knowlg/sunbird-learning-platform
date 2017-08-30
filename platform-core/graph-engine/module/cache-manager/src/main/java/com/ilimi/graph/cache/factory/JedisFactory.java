package com.ilimi.graph.cache.factory;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.ilimi.common.Platform;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;

public class JedisFactory {

	private static JedisPool jedisPool;

	private static int maxConnections = 128;
	private static String host = "localhost";
	private static int port = 6379;
	private static int index = 0;

	static {
		if (Platform.config.hasPath("redis.host")) host = Platform.config.getString("redis.host");
		if (Platform.config.hasPath("redis.port")) port = Platform.config.getInt("redis.port");
		if (Platform.config.hasPath("redis.maxConnections")) maxConnections = Platform.config.getInt("redis.maxConnections");
		if (Platform.config.hasPath("redis.dbIndex")) index = Platform.config.getInt("redis.dbIndex");
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxConnections);
		config.setBlockWhenExhausted(true);
		jedisPool = new JedisPool(config, host, port);
	}

	private static boolean isConnected() {
		if (null != jedisPool) {
			try {
				jedisPool.getResource();
				return true;
			} catch (JedisConnectionException ex) {
				System.out.println("No connection found");
			}
		}
		return false;
	}

	public static void initialize(Map<String, Object> props) throws Exception {

		String redisHost = (String) props.get("redis.host");
		String redisPort = (String) props.get("redis.port");
		String redisMaxConn = (String) props.get("redis.maxConnections");
		String dbIndex = (String) props.get("redis.dbIndex");
		if (!isConnected() && StringUtils.isNotBlank(redisHost) && StringUtils.isNotBlank(redisPort)) {
			// Seems like redis is not initialized
			port = NumberUtils.toInt(redisPort);
			maxConnections = NumberUtils.isNumber(redisMaxConn) ? NumberUtils.toInt(redisMaxConn) : maxConnections;
			index = NumberUtils.isNumber(dbIndex) ? NumberUtils.toInt(dbIndex) : index;
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(maxConnections);
			config.setBlockWhenExhausted(true);
			jedisPool = new JedisPool(config, redisHost, port);
			try {
				jedisPool.getResource();
			} catch (JedisConnectionException ex) {
				throw ex;
			}
		}
	}

	public static Jedis getRedisConncetion() {
		try {
			Jedis jedis = jedisPool.getResource();
			if (index > 0)
				jedis.select(index);
			return jedis;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
		}

	}

	public static void returnConnection(Jedis jedis) {
		try {
			if (null != jedis)
				jedisPool.returnResource(jedis);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
		}
	}
}
