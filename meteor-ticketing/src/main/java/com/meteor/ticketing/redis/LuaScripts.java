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

    public static final String GRAB_SEM_TRY_ACQUIRE = """
    -- KEYS[1]=permitsKey, KEYS[2]=maxKey, KEYS[3]=leaseZset
    -- ARGV[1]=ttlMs, ARGV[2]=token
    local permits = tonumber(redis.call('GET', KEYS[1]) or '0')
    if permits <= 0 then
      return 0
    end

    redis.call('DECR', KEYS[1])

    local t = redis.call('TIME')
    local nowMs = t[1] * 1000 + math.floor(t[2] / 1000)
    local expMs = nowMs + tonumber(ARGV[1])

    redis.call('ZADD', KEYS[3], expMs, ARGV[2])

    return expMs
    """;

    public static final String GRAB_SEM_RELEASE = """
    -- KEYS[1]=permitsKey, KEYS[2]=maxKey, KEYS[3]=leaseZset
    -- ARGV[1]=token
    local removed = redis.call('ZREM', KEYS[3], ARGV[1])
    if removed == 0 then
      return 0
    end

    local maxv = tonumber(redis.call('GET', KEYS[2]) or '0')
    local cur = tonumber(redis.call('GET', KEYS[1]) or '0')
    if cur < maxv then
      redis.call('INCR', KEYS[1])
    end
    return 1
    """;

    public static final String GRAB_SEM_RECLAIM_EXPIRED = """
    -- KEYS[1]=permitsKey, KEYS[2]=maxKey, KEYS[3]=leaseZset
    -- ARGV[1]=batchSize
    local t = redis.call('TIME')
    local nowMs = t[1] * 1000 + math.floor(t[2] / 1000)
    
    local batch = tonumber(ARGV[1])
    local expired = redis.call('ZRANGEBYSCORE', KEYS[3], '-inf', nowMs, 'LIMIT', 0, batch)
    local n = #expired
    if n == 0 then
      return 0
    end
    
    redis.call('ZREM', KEYS[3], unpack(expired))
    
    local maxv = tonumber(redis.call('GET', KEYS[2]) or '0')
    local cur = tonumber(redis.call('GET', KEYS[1]) or '0')
    local canAdd = maxv - cur
    if canAdd <= 0 then
      return n
    end
    
    local add = n
    if add > canAdd then add = canAdd end
    redis.call('INCRBY', KEYS[1], add)
    
    return n
    """;

    public static final String RESOLVE_GRAB_REQUEST_ID = """
    -- KEYS[1] = requestKey
    -- KEYS[2] = readyKey
    -- KEYS[3] = saleEndKey
    
    -- ARGV[1] = candidateRequestId
    -- ARGV[2] = fingerprint
    -- ARGV[3] = ttlMillis

    local existingRequestId = redis.call('HGET', KEYS[1], 'requestId')

    if existingRequestId then
        local existingFingerprint = redis.call('HGET', KEYS[1], 'fingerprint')

        if existingFingerprint ~= ARGV[2] then
            return '__CONFLICT__'
        end

        return existingRequestId
    end
    
    local saleEndEpoch = redis.call('GET', KEYS[3])
    
    if not saleEndEpoch then
        return '__NOT_READY__'
    end
    
    local redisTime = redis.call('TIME')
    local nowEpoch = tonumber(redisTime[1])
    
    if nowEpoch >= tonumber(saleEndEpoch) then
        return '__SALE_CLOSED__'
    end

    redis.call(
        'HSET',
        KEYS[1],
        'requestId', ARGV[1],
        'fingerprint', ARGV[2]
    )

    local ttlMillis = tonumber(ARGV[3])

    if ttlMillis and ttlMillis > 0 then
        redis.call('PEXPIRE', KEYS[1], ttlMillis)
    end

    return ARGV[1]
    """;
}
