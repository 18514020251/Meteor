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
    local stock = redis.call('GET', KEYS[1])
    if not stock then
        return -3
    end

    stock = tonumber(stock)
    if stock <= 0 then
        return -1
    end

    local left = redis.call('DECR', KEYS[1])
    return left
    """;

    public static final String INCR_STOCK_N = """
    local stock = redis.call('GET', KEYS[1])
    if not stock then
        return -3
    end

    local n = tonumber(ARGV[1])
    if not n or n <= 0 then
        return -2
    end

    local left = redis.call('INCRBY', KEYS[1], n)
    return left
    """;
}
