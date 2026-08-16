package com.meteor.ticketing.service.cache;

import com.meteor.ticketing.service.cache.model.RedisStockOpResult;

/**
 *  缓存服务 接口
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 21:55
 */
public interface ITicketingStockRedisService {

    boolean isSaleStarted(Long screeningId);

    /**
     * 旧版 Redis 可售库存扣减原语。
     *
     * <p>用于 M1A 及 Reservation 接入前的抢票库存裁决。
     *
     * <p>注意：
     * M1B-05 开始后，GrabOrderServiceImpl 主链将逐步停止直接调用
     * decrStock1()，改由 RESERVE_TICKET 将：
     *
     * reservationId 幂等判断
     * +
     * 库存判断
     * +
     * 库存扣减
     * +
     * Reservation 创建
     *
     * 放在同一 Lua 原子执行。
     *
     * <p>因此本脚本后续可能仍被其他库存场景使用，
     * 但不再作为抢票 Reservation 主链的最终库存语义。
     */
    RedisStockOpResult decrStock1(Long screeningId);

    /**
     * 增量增加 Redis 可售库存。
     * quantity 表示变化量，不表示完整库存。
     */
    RedisStockOpResult increaseAvailableStock(Long screeningId, int quantity);

    /**
     * 使用完整库存快照重建 Redis。
     * availableStock 必须来自权威状态，
     * 不得传入本次增量 quantity。
     */
    void rebuildStockFromSnapshot(Long screeningId, int availableStock);

}
