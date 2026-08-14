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

    public static final DefaultRedisScript<Long> GRAB_SEM_TRY_ACQUIRE;

    static {
        GRAB_SEM_TRY_ACQUIRE = new DefaultRedisScript<>();
        GRAB_SEM_TRY_ACQUIRE.setScriptText(LuaScripts.GRAB_SEM_TRY_ACQUIRE);
        GRAB_SEM_TRY_ACQUIRE.setResultType(Long.class);
    }

    public static final DefaultRedisScript<Long> GRAB_SEM_RELEASE;
    static {
        GRAB_SEM_RELEASE = new DefaultRedisScript<>();
        GRAB_SEM_RELEASE.setScriptText(LuaScripts.GRAB_SEM_RELEASE);
        GRAB_SEM_RELEASE.setResultType(Long.class);
    }

    public static final DefaultRedisScript<Long> GRAB_SEM_RECLAIM_EXPIRED;
    static {
        GRAB_SEM_RECLAIM_EXPIRED = new DefaultRedisScript<>();
        GRAB_SEM_RECLAIM_EXPIRED.setScriptText(LuaScripts.GRAB_SEM_RECLAIM_EXPIRED);
        GRAB_SEM_RECLAIM_EXPIRED.setResultType(Long.class);
    }

    public static final DefaultRedisScript<String> RESOLVE_GRAB_REQUEST_ID;
    static {
        RESOLVE_GRAB_REQUEST_ID = new DefaultRedisScript<>();
        RESOLVE_GRAB_REQUEST_ID.setScriptText(LuaScripts.RESOLVE_GRAB_REQUEST_ID);
        RESOLVE_GRAB_REQUEST_ID.setResultType(String.class);
    }
}