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

    /**
     * 扣减库存（扣1）
     * return:
     *  >=0  剩余库存
     *  -1   库存不足
     *  -3   库存不存在（未预热）
     */
    public static final DefaultRedisScript<Long> DECR_STOCK_1;

    static {
        DECR_STOCK_1 = new DefaultRedisScript<>();
        DECR_STOCK_1.setScriptText(LuaScripts.DECR_STOCK_1);
        DECR_STOCK_1.setResultType(Long.class);
    }
}