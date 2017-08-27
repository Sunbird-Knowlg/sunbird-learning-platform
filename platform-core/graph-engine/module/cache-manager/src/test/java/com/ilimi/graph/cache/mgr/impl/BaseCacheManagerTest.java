package com.ilimi.graph.cache.mgr.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import redis.embedded.RedisServer;

public class BaseCacheManagerTest {

	private static RedisServer redisServer = null;
	
	@BeforeClass
	public static void beforeAll() {
		try {
			redisServer = new RedisServer(6379);
			redisServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void afterAll() {
		if (null != redisServer)
			redisServer.stop();
	}
	
	
}
