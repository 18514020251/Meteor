package com.meteor.ticketing.redis;

import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Ticketing Redis Lua 脚本注册中心。
 *
 * <p>这里只负责将 LuaScripts 中的脚本文本注册为
 * Spring Data Redis 可执行的 DefaultRedisScript。
 *
 * <p>业务语义、KEYS / ARGV 和返回码说明统一维护在 LuaScripts。
 *
 * <p>业务 Service 不应直接依赖 Lua 数字返回码，
 * 应通过对应 Java 门面转换成领域结果。
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

    public static final DefaultRedisScript<Long> RELEASE_LOCK_IF_OWNER;
    static {
        RELEASE_LOCK_IF_OWNER = new DefaultRedisScript<>();
        RELEASE_LOCK_IF_OWNER.setScriptText(LuaScripts.RELEASE_LOCK_IF_OWNER);
        RELEASE_LOCK_IF_OWNER.setResultType(Long.class);
    }

    public static final DefaultRedisScript<Long> RESERVE_TICKET;
    static {
        RESERVE_TICKET = new DefaultRedisScript<>();
        RESERVE_TICKET.setScriptText(LuaScripts.RESERVE_TICKET);
        RESERVE_TICKET.setResultType(Long.class);
    }

    public static final DefaultRedisScript<Long> RELEASE_RESERVATION;
    static {
        RELEASE_RESERVATION = new DefaultRedisScript<>();
        RELEASE_RESERVATION.setScriptText(LuaScripts.RELEASE_RESERVATION);
        RELEASE_RESERVATION.setResultType(Long.class);
    }

    public static final DefaultRedisScript<Long> CONFIRM_RESERVATION;
    static {
        CONFIRM_RESERVATION = new DefaultRedisScript<>();
        CONFIRM_RESERVATION.setScriptText(LuaScripts.CONFIRM_RESERVATION);
        CONFIRM_RESERVATION.setResultType(Long.class);
    }
}