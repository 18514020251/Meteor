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

    RedisStockOpResult incrStockN(Long screeningId, int cnt);

    void rebuildStock(Long screeningId, int cnt);

}
