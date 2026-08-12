package com.meteor.ticketing.service.impl;

import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.service.IScreeningService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 *  库存恢复服务实现类测试
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-12
 */
@ExtendWith(MockitoExtension.class)
class StockRecoveryServiceImplTest {

    @Mock
    private IScreeningService screeningService;

    @Mock
    private ITicketingStockRedisService stockRedisService;


    private StockRecoveryServiceImpl recoveryService;

    @BeforeEach
    void setUp() {
        recoveryService = new StockRecoveryServiceImpl(
                screeningService,
                stockRedisService
        );
    }

    @DisplayName("重建 Redis 库存时，应使用数据库权威可售库存")
    @Test
    void shouldRebuildRedisStockFromAuthoritativeAvailableStock() {
        Long screeningId = 2001L;
        Integer authoritativeStock = 83;

        Screening screening = new Screening()
                .setId(screeningId)
                .setAvailableTickets(authoritativeStock);

        when(screeningService.getById(screeningId)).thenReturn(screening);

        recoveryService.rebuildFromAuthoritativeState(screeningId);

        verify(stockRedisService).rebuildStockFromSnapshot(screeningId, authoritativeStock);
    }

    @DisplayName("重建 Redis 库存时，若数据库中不存在该场次则应抛出异常")
    @Test
    void shouldFailWhenScreeningDoesNotExist() {

        Long screeningId = 2001L;

        when(screeningService.getById(screeningId))
                .thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> recoveryService
                        .rebuildFromAuthoritativeState(screeningId)
        );

        verify(stockRedisService, never())
                .rebuildStockFromSnapshot(anyLong(), anyInt());
    }

    @DisplayName("重建 Redis 库存时，若数据库中存在该场次但权威可售库存为负数则应抛出异常")
    @Test
    void shouldFailWhenAuthoritativeStockIsNegative() {

        Long screeningId = 2001L;

        Screening screening = new Screening()
                .setId(screeningId)
                .setAvailableTickets(-1);

        when(screeningService.getById(screeningId))
                .thenReturn(screening);

        assertThrows(
                IllegalStateException.class,
                () -> recoveryService
                        .rebuildFromAuthoritativeState(screeningId)
        );

        verify(stockRedisService, never())
                .rebuildStockFromSnapshot(anyLong(), anyInt());
    }


}
