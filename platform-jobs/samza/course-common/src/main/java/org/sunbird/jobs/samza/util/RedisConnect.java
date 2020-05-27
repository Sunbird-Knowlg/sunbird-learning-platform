package org.sunbird.jobs.samza.util;

import org.apache.samza.config.Config;
import redis.clients.jedis.Jedis;

public class RedisConnect {

    private Config config;

    public RedisConnect(Config config) {
        this.config = config;
    }

    private Jedis getConnection(long backoffTimeInMillis) {
        String redisHost = config.get("redis.host", "localhost");
        Integer redisPort = config.getInt("redis.port", 6379);
        if(backoffTimeInMillis > 0) {
            try {
                Thread.sleep(backoffTimeInMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new Jedis(redisHost, redisPort, 30000);
    }

    public Jedis getConnection(int db, long backoffTimeInMillis) {

        Jedis jedis = getConnection(backoffTimeInMillis);
        jedis.select(db);
        return jedis;
    }

    public Jedis getConnection() {
        return getConnection(0);
    }
}
