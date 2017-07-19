package com.ilimi.graph.cache.factory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.common.mgr.Configuration;

public class JedisFactory {

	private static JedisPool jedisPool;

	private static int maxConnections = 128;
	private static String host = "localhost";
	private static int port = 6379;
	private static int index = 0;

	static {
		try (InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream("graph.properties")) {
			if (null != inputStream) {
				Properties props = new Properties();
				props.load(inputStream);
				String redisHost = props.getProperty("redis.host");
				if (StringUtils.isNotBlank(redisHost))
					host = redisHost;
				String redisPort = props.getProperty("redis.port");
				if (StringUtils.isNotBlank(redisPort)) {
					try {
						port = Integer.parseInt(redisPort);
					} catch (Exception e) {
					}
				}
				String redisMaxConn = props.getProperty("redis.maxConnections");
				if (StringUtils.isNotBlank(redisMaxConn)) {
					try {
						maxConnections = Integer.parseInt(redisMaxConn);
					} catch (Exception e) {
					}
				}
				String dbIndex = props.getProperty("redis.dbIndex");
				if (StringUtils.isNotBlank(dbIndex)) {
					try {
						index = Integer.parseInt(dbIndex);
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			PlatformLogger.log("Error! While Loading Graph Properties.", null, e);
		}
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
			index = NumberUtils.isNumber(dbIndex) ? NumberUtils.toInt(dbIndex) : maxConnections;
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
