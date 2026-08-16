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

    /**
     * 原子解析抢票请求的稳定业务身份 requestId。
     *
     * <p>核心目标：
     * 同一 userId + clientRequestId，无论请求重试多少次，
     * 都只能对应同一个 requestId。
     *
     * <p>幂等规则：
     * 1. 如果请求身份已经存在：
     *    - fingerprint 相同：返回原 requestId；
     *    - fingerprint 不同：返回 CONFLICT；
     * 2. 只有第一次请求才检查销售窗口并创建 requestId；
     * 3. 已存在的请求即使在 saleEnd 后重试，
     *    仍然允许取回原 requestId。
     *
     * <p>Redis Key：
     * KEYS[1] = grabRequestKey
     *            grab:request:{userId}:{clientRequestId}
     * KEYS[2] = readyKey
     * KEYS[3] = saleEndKey
     *
     * <p>ARGV：
     * ARGV[1] = candidateRequestId
     * ARGV[2] = fingerprint
     * ARGV[3...] = 以当前 Lua 实际定义为准
     *
     * <p>特殊返回值：
     * __CONFLICT__    = 同一 clientRequestId 被用于不同业务参数
     * __SALE_CLOSED__ = 新请求到达时已经停售
     * __NOT_READY__   = 场次尚未准备完成
     *
     * <p>注意：
     * 这里解决的是“请求身份幂等”，
     * 并不负责库存扣减或 Reservation 幂等。
     */
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

    /**
     * 仅允许锁的当前 owner 释放 Redis 预热锁。
     *
     * <p>Redis Key：
     * KEYS[1] = warmupLockKey
     *
     * <p>ARGV：
     * ARGV[1] = ownerToken
     *
     * <p>返回值：
     * 1 = 当前调用者仍是锁 owner，成功删除锁
     * 0 = 锁不存在或 ownerToken 已变化，不执行删除
     *
     * <p>为什么不能直接 DEL：
     *
     * owner A 获取锁
     *      ->
     * TTL 到期
     *      ->
     * owner B 获取同一个锁
     *      ->
     * owner A 工作结束
     *
     * 如果 A 此时直接 DEL，
     * 会错误删除 B 的新锁。
     *
     * 因此必须原子执行：
     *
     * GET lockKey == ownerToken
     *      ->
     * DEL lockKey
     *
     * TTL 负责死锁兜底，
     * ownerToken + Lua 负责安全释放。
     */
    public static final String RELEASE_LOCK_IF_OWNER = """
    -- KEYS[1] = lockKey
    -- ARGV[1] = lockToken

    if redis.call('GET', KEYS[1]) == ARGV[1] then
        return redis.call('DEL', KEYS[1])
    end

    return 0
    """;

    /**
     * 原子创建 Redis Reservation 并预留库存。
     *
     * <p>这是抢票库存预留的核心原子操作。
     * 在一次 Redis Lua 执行中完成：
     *
     * 1. reservationId 幂等检查；
     * 2. quantity 合法性检查；
     * 3. 销售开始/结束时间检查；
     * 4. Redis stock 是否存在；
     * 5. 可售库存是否充足；
     * 6. stock -= quantity；
     * 7. 创建 PRE_RESERVED Reservation。
     *
     * <p>Redis Key：
     * KEYS[1] = stockKey
     * KEYS[2] = reservationKey
     * KEYS[3] = readyKey / saleStartKey
     * KEYS[4] = saleEndKey
     *
     * <p>ARGV：
     * ARGV[1] = screeningId
     * ARGV[2] = quantity
     *
     * <p>返回码：
     *  1 = 本次真正预留成功，库存已扣减
     *  2 = reservationId 已存在，幂等重放，不重复扣库存
     * -1 = 库存不足
     * -2 = quantity 非法
     * -3 = 必需的 Redis 元数据或 stock 缺失
     * -4 = 尚未开始销售
     * -5 = 已经停止销售
     *
     * <p>关键语义：
     * Reservation 存在检查必须先于销售窗口检查。
     * 因此停售前已成功创建的 Reservation，
     * 在停售后重试仍然识别为幂等请求，而不是 SALE_CLOSED。
     *
     * <p>生命周期：
     * PRE_RESERVED 不允许依靠 Redis TTL 静默删除。
     * 必须通过 confirm / release / compensate 显式离开。
     *
     * <p>原子性：
     * Reservation 判断、库存判断、库存扣减和 Reservation 创建
     * 全部由同一 Lua 完成，不需要额外分布式锁。
     */
    public static final String RESERVE_TICKET = """
    -- KEYS[1] = stockKey
    -- KEYS[2] = reservationKey
    -- KEYS[3] = readyKey / saleStartKey
    -- KEYS[4] = saleEndKey
    --
    -- ARGV[1] = screeningId
    -- ARGV[2] = quantity

    local status = redis.call('HGET', KEYS[2], 'status')

    -- 已存在 Reservation：幂等重放，不再次扣库存。
    if status then
        return {2, -1}
    end

    local quantity = tonumber(ARGV[2])
    if not quantity or quantity <= 0 then
        return {-2, -1}
    end

    local saleStart = redis.call('GET', KEYS[3])
    local saleEnd = redis.call('GET', KEYS[4])
    if not saleStart or not saleEnd then
        return {-3, -1}
    end

    local now = tonumber(redis.call('TIME')[1])
    saleStart = tonumber(saleStart)
    saleEnd = tonumber(saleEnd)

    if now < saleStart then
        return {-4, -1}
    end
    if now > saleEnd then
        return {-5, -1}
    end

    local stock = redis.call('GET', KEYS[1])
    if not stock then
        return {-3, -1}
    end
    stock = tonumber(stock)

    if stock < quantity then
        return {-1, -1}
    end

    local leftStock = redis.call('DECRBY', KEYS[1], quantity)

    redis.call(
        'HSET',
        KEYS[2],
        'status', 'PRE_RESERVED',
        'screeningId', ARGV[1],
        'quantity', ARGV[2]
    )

    return {1, leftStock}
    """;

    /**
     * 原子释放或补偿 PRE_RESERVED Reservation。
     *
     * <p>支持两个目标终态：
     *
     * PRE_RESERVED -> RELEASED
     * PRE_RESERVED -> COMPENSATED
     *
     * <p>同时根据 Reservation 中记录的 quantity
     * 恢复对应 Redis 可售库存。
     *
     * <p>Redis Key：
     * KEYS[1] = stockKey
     * KEYS[2] = reservationKey
     *
     * <p>ARGV：
     * ARGV[1] = targetStatus
     *            RELEASED / COMPENSATED
     *
     * <p>返回码：
     *  1 = 本次真正执行状态转换，并恢复库存
     *  2 = 已经处于目标状态，幂等重放，不重复恢复库存
     * -1 = Reservation 不存在
     * -2 = targetStatus 非法
     * -3 = 当前 Reservation 状态不允许释放
     * -4 = Reservation quantity 缺失或非法
     * -5 = stock key 缺失
     *
     * <p>关键语义：
     * 只有 PRE_RESERVED 才允许恢复库存。
     *
     * CONFIRMED -> RELEASED        禁止
     * CONFIRMED -> COMPENSATED     禁止
     * RELEASED -> COMPENSATED      禁止
     * COMPENSATED -> RELEASED      禁止
     *
     * <p>库存恢复数量必须读取 Reservation 自身保存的 quantity，
     * 不能由调用者再次传入，避免错误数量导致库存被多恢复。
     *
     * <p>stock key 缺失时禁止直接执行 INCRBY。
     * Redis 对不存在的 key 执行 INCRBY 会自动创建 key，
     * 这会把“库存状态丢失”错误伪装成一个新的库存数字。
     *
     * <p>原子性：
     * 状态判断、库存恢复和终态写入由同一 Lua 完成，
     * 重复补偿不会重复增加库存。
     */
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

    /**
     * 原子确认 PRE_RESERVED Reservation。
     *
     * <p>状态转换：
     *
     * PRE_RESERVED -> CONFIRMED
     *
     * <p>Redis Key：
     * KEYS[1] = reservationKey
     *
     * <p>ARGV：
     * 无。
     *
     * <p>返回码：
     *  1 = 本次真正完成 PRE_RESERVED -> CONFIRMED
     *  2 = 已经 CONFIRMED，属于幂等重放
     * -1 = Reservation 不存在
     * -2 = 当前状态不允许 confirm
     *
     * <p>关键语义：
     * confirm 只修改 Reservation 状态，不再次扣减 Redis 可售库存。
     * 可售库存已经在 reserve 阶段完成扣减。
     *
     * RELEASED -> CONFIRMED      禁止
     * COMPENSATED -> CONFIRMED   禁止
     *
     * <p>重复 confirm 不产生任何额外副作用。
     */
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
