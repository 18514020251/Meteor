package com.meteor.ticketing.service.impl;

import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.service.IScreeningService;
import com.meteor.ticketing.service.IStockRecoveryService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *  库存恢复服务实现类
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-12
 */
@Service
@RequiredArgsConstructor
public class StockRecoveryServiceImpl implements IStockRecoveryService {

    private final IScreeningService screeningService;
    private final ITicketingStockRedisService stockRedisService;

    @Override
    public void rebuildFromAuthoritativeState(Long screeningId) {

        Screening screening = screeningService.getById(screeningId);

        if (screening == null) {
            throw new IllegalStateException("未找到筛查: " + screeningId);
        }

        Integer availableTickets = screening.getAvailableTickets();

        if (availableTickets == null || availableTickets < 0) {
            throw new IllegalStateException("无效的可用库存: " + availableTickets);
        }

        stockRedisService.rebuildStockFromSnapshot(screeningId, availableTickets);
    }
}
