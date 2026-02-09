package com.meteor.ticketing.redis;

/**
 *  Redis Lua 脚本集中管理
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:33
 */
public final class LuaScripts {

    private LuaScripts() {}

    public static final String DECR_STOCK_1 = """
        local stock = tonumber(redis.call('GET', KEYS[1]))
        if not stock then
            return -3
        end

        if stock <= 0 then
            return -1
        end

        stock = stock - 1
        redis.call('SET', KEYS[1], stock)

        return stock
        """;
}
