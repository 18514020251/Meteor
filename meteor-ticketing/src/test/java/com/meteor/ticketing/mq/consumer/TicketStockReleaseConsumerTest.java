package com.meteor.ticketing.mq.consumer;

import com.meteor.mq.contract.ticketing.TicketStockReleaseMessage;
import com.meteor.ticketing.enums.RedisStockResultEnum;
import com.meteor.ticketing.mapper.TicketMqConsumeLogMapper;
import com.meteor.ticketing.service.IScreeningService;
import com.meteor.ticketing.service.IStockRecoveryService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.cache.model.RedisStockOpResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * 票务库存释放消费者测试类
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-12
 */
@ExtendWith(MockitoExtension.class)
class TicketStockReleaseConsumerTest {

    @Mock
    private TicketMqConsumeLogMapper consumeLogMapper;

    @Mock
    private IScreeningService screeningStockService;

    @Mock
    private ITicketingStockRedisService stockRedisService;

    @Mock
    private IStockRecoveryService stockRecoveryService;

    private TicketStockReleaseConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TicketStockReleaseConsumer(
                consumeLogMapper,
                screeningStockService,
                stockRedisService,
                stockRecoveryService
        );
    }

    @DisplayName("Redis 库存 Key 缺失时，不应使用本次释放数量重建完整库存")
    @Test
    void shouldNotRebuildFullStockWithReleaseCountWhenRedisKeyMissing() {
        // Arrange
        Long screeningId = 2001L;
        int releaseCount = 1;

        TicketStockReleaseMessage message =
                new TicketStockReleaseMessage();

        message.setOrderNo("900001");
        message.setScreeningId(screeningId);
        message.setTicketCount(releaseCount);

        when(screeningStockService.incrStockAndDecrSold(
                screeningId,
                releaseCount
        )).thenReturn(true);

        RedisStockOpResult notReadyResult =
                new RedisStockOpResult(
                        RedisStockResultEnum.NOT_READY,
                        -3L
                );

        when(stockRedisService.increaseAvailableStock(
                screeningId,
                releaseCount
        )).thenReturn(notReadyResult);

        // Act
        consumer.handle(message);

        // Assert
        verify(stockRecoveryService)
                .rebuildFromAuthoritativeState(screeningId);
    }

    @DisplayName("Redis 库存 Key 正常时，应只增加本次释放数量")
    @Test
    void shouldIncreaseOnlyReleasedQuantityWhenRedisStockExists() {
        // Arrange
        Long screeningId = 2001L;
        int releaseCount = 1;

        TicketStockReleaseMessage message = new TicketStockReleaseMessage();

        message.setOrderNo("900001");
        message.setScreeningId(screeningId);
        message.setTicketCount(releaseCount);

        when(screeningStockService.incrStockAndDecrSold(screeningId, releaseCount))
                .thenReturn(true);

        when(stockRedisService.increaseAvailableStock(screeningId, releaseCount))
                .thenReturn(
                new RedisStockOpResult(
                        RedisStockResultEnum.SUCCESS,
                        83L
                )
        );

        // Act
        consumer.handle(message);

        // Assert
        verify(stockRedisService)
                .increaseAvailableStock(
                        screeningId,
                        releaseCount
                );

        verifyNoInteractions(stockRecoveryService);
    }
}
