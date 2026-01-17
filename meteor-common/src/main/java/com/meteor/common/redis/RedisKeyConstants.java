package com.meteor.common.redis;

import java.time.Duration;

/**
 * Redis 常量
 *
 * @author Programmer
 * @date 2026-01-17 19:19
 */
public class RedisKeyConstants {

    // 用户模块
    public static final String USER_INFO_KEY = "user:info:%s";

    public static final Duration USER_INFO_TTL = Duration.ofHours(1);
    public static final Duration USER_INFO_NULL_TTL = Duration.ofSeconds(60);

    public static final String CACHE_NULL_VALUE = "__NULL__";


}
