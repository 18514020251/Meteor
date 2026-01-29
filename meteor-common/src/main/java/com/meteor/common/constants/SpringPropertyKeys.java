package com.meteor.common.constants;

/**
 *  Redis 启动连接测试常量
 *
 * @author Programmer
 * @date 2026-01-26 22:03
 */
public class SpringPropertyKeys {
    private SpringPropertyKeys() {}

    public static final String REDIS_HOST = "spring.data.redis.host";
    public static final String REDIS_PORT = "spring.data.redis.port";
    public static final String REDIS_DB   = "spring.data.redis.database";

    public static final String REDIS_DEFAULT_DB = "0";
}
