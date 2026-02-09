package com.meteor.ticketing.redis;

import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 *  Redis 脚本对象集中管理
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:41
 */
public final class RedisScripts {

    private RedisScripts() {}

    public static final DefaultRedisScript<Long> DECR_STOCK_1;

    static {
        DECR_STOCK_1 = new DefaultRedisScript<>();
        DECR_STOCK_1.setScriptText(LuaScripts.DECR_STOCK_1);
        DECR_STOCK_1.setResultType(Long.class);
    }

    public static final DefaultRedisScript<Long> INCR_STOCK_N;

    static {
        INCR_STOCK_N = new DefaultRedisScript<>();
        INCR_STOCK_N.setScriptText(LuaScripts.INCR_STOCK_N);
        INCR_STOCK_N.setResultType(Long.class);
    }
}