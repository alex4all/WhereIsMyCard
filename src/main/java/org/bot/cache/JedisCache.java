package org.bot.cache;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisCache {
    private static final String HOST = System.getenv("redis_host");
    private static final String PASS = System.getenv("redis_password");
    private static final int PORT = 6379;
    private static final int TIMEOUT = 2000;

    private JedisPool pool;
    private static final JedisCache INSTANCE = new JedisCache();

    private JedisCache() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(5);
        config.setMaxTotal(15);
        config.setMinIdle(3);
        if (PASS != null)
            pool = new JedisPool(config, HOST, PORT, TIMEOUT, PASS);
        else
            pool = new JedisPool(config, HOST, PORT, TIMEOUT);
    }

    public static JedisCache getInstance() {
        return INSTANCE;
    }

    public Jedis getConnection() {
        return pool.getResource();
    }
}
