package com.ilimi.graph.cache.factory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
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