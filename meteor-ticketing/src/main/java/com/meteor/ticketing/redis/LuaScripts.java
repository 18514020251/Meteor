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

    public static final String RELEASE_LOCK_IF_OWNER = """
    -- KEYS[1] = lockKey
    -- ARGV[1] = lockToken

    if redis.call('GET', KEYS[1]) == ARGV[1] then
        return redis.call('DEL', KEYS[1])
    end

    return 0
    """;

    public static final String RESERVE_TICKET = """
    -- KEYS[1] = stockKey
    -- KEYS[2] = reservationKey
    -- KEYS[4] = saleEndKey
    --
    -- ARGV[1] = screeningId
    -- ARGV[2] = quantity
    -- ARGV[3] = reservationTtlMillis

    -- 1. reservation 已存在时，本次属于幂等重放。
    --    必须放在扣库存之前。
    local existingStatus = redis.call('HGET', KEYS[2], 'status')
    if existingStatus then
        return 2
    end

    -- 2. 校验 quantity。
    local quantity = tonumber(ARGV[2])
    if not quantity or quantity <= 0 then
        return -2
    end

    -- 3. 获取销售窗口。
    local saleStartEpoch = redis.call('GET', KEYS[3])
    local saleEndEpoch = redis.call('GET', KEYS[4])
    if not saleStartEpoch or not saleEndEpoch then
        return -3
    end

    -- 4. Redis TIME 作为统一时钟。
    local redisTime = redis.call('TIME')
    local nowEpoch = tonumber(redisTime[1])
    if nowEpoch < tonumber(saleStartEpoch) then
        return -4
    end
    if nowEpoch >= tonumber(saleEndEpoch) then
        return -5
    end

    -- 5. 校验库存。
    local stock = redis.call('GET', KEYS[1])
    if not stock then
        return -3
    end
    stock = tonumber(stock)
    if stock < quantity then
        return -1
    end

    -- 6. 原子扣减库存。
    redis.call('DECRBY', KEYS[1], quantity)

    -- 7. 登记 PRE_RESERVED。
    redis.call('HSET', KEYS[2],
        'status', 'PRE_RESERVED',
        'screeningId', ARGV[1],
        'quantity', ARGV[2]
    )

    return 1
    """;

    public static final String RELEASE_RESERVATION = """
    -- KEYS[1] = stockKey
    -- KEYS[2] = reservationKey
    --
    -- ARGV[1] = targetStatus
    --           RELEASED / COMPENSATED

    local status = redis.call('HGET', KEYS[2], 'status')

    -- Reservation 根本不存在。
    if not status then
        return -1
    end

    local targetStatus = ARGV[1]

    -- 只允许释放到两个合法终态。
    if targetStatus ~= 'RELEASED' and targetStatus ~= 'COMPENSATED' then
        return -2
    end

    -- 已经处于目标终态：
    -- 本次属于幂等重放。
    if status == targetStatus then
        return 2
    end

    -- 只有 PRE_RESERVED
    -- 才能够执行真正的库存恢复。
    --
    -- 例如：
    -- CONFIRMED -> RELEASED
    -- RELEASED -> COMPENSATED
    -- 都禁止。
    if status ~= 'PRE_RESERVED' then
        return -3
    end

    local quantity = tonumber(redis.call('HGET', KEYS[2], 'quantity'))
    if not quantity or quantity <= 0 then
        return -4
    end

    -- 非常重要：
    --
    -- Redis stock key 如果丢失，
    -- 不能直接 INCRBY。
    --
    -- 因为 INCRBY 一个不存在的 key
    -- 会自动创建该 key。
    --
    -- 那样可能把：
    -- "库存缓存丢失"
    -- 错误变成
    -- "库存 = reservation.quantity"。
    local stock = redis.call('GET', KEYS[1])
    if not stock then
        return -5
    end

    -- 库存恢复 + 状态转换
    -- 必须在同一 Lua 内原子完成。
    redis.call('INCRBY', KEYS[1], quantity)
    redis.call('HSET', KEYS[2], 'status', targetStatus)

    return 1
    """;

    public static final String CONFIRM_RESERVATION = """
    -- KEYS[1] = reservationKey

    local status = redis.call('HGET', KEYS[1], 'status')

    -- Reservation 不存在
    if not status then
        return -1
    end

    -- 已经确认，幂等重放
    if status == 'CONFIRMED' then
        return 2
    end

    -- 只有 PRE_RESERVED 可以确认
    if status ~= 'PRE_RESERVED' then
        return -2
    end

    redis.call('HSET', KEYS[1], 'status', 'CONFIRMED')

    return 1
    """;
}
