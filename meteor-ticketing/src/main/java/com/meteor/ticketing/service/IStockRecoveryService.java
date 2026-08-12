package com.meteor.ticketing.service;

/**
 *  库存恢复服务接口
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-12
 */
public interface IStockRecoveryService {

    void rebuildFromAuthoritativeState(Long screeningId);
}
