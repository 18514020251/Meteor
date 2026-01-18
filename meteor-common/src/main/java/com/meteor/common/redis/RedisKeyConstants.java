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
    public static final String USER_INFO_KEY = "user:info:%d";

    public static final Duration USER_INFO_TTL = Duration.ofHours(1);
    public static final Duration USER_INFO_NULL_TTL = Duration.ofSeconds(60);

    public static final String CACHE_NULL_VALUE = "__NULL__";

    /*
    *  用户信息缓存 TTL 随机抖动上限（秒）
    * */
    public static final long USER_INFO_TTL_RANDOM = 20L;


}
