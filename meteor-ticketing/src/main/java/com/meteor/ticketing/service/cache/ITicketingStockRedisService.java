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
