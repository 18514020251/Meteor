package com.meteor.ticketing.mq.consumer;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketStockReleaseMessage;
import com.meteor.ticketing.mapper.TicketMqConsumeLogMapper;
import com.meteor.ticketing.service.IScreeningService;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.cache.model.RedisStockOpResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 *  订单库存释放消费者
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 20:45
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketStockReleaseConsumer {

    private final TicketMqConsumeLogMapper consumeLogMapper;
    private final IScreeningService screeningStockService;
    private final ITicketingStockRedisService stockRedisService;

    private static final String TOPIC = TicketOrderContract.RoutingKey.TICKET_STOCK_RELEASE;

    @RabbitListener(
            queues = TicketOrderContract.Queue.TICKET_STOCK_RELEASE,
            errorHandler = "mqRejectErrorHandler"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handle(TicketStockReleaseMessage message) {

        if (message == null
                || message.getOrderNo() == null
                || message.getScreeningId() == null) {
            return;
        }

        Integer cnt = message.getTicketCount();
        if (cnt == null || cnt <= 0) {
            return;
        }

        try {
            consumeLogMapper.insert(
                    message.getOrderNo(),
                    TOPIC,
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            return;
        }

        boolean ok = screeningStockService.incrStockAndDecrSold(message.getScreeningId(), cnt);
        if (!ok) {
            throw new IllegalStateException("screening stock release failed");
        }

        screeningStockService.markSellingIfHasStock(message.getScreeningId());

        RedisStockOpResult r = stockRedisService.incrStockN(message.getScreeningId(), cnt);

        switch (r.code()) {

            case SUCCESS -> log.info(
                    "[StockReleaseOK] orderNo={} screeningId={} +{} left={}",
                    message.getOrderNo(), message.getScreeningId(), cnt, r.left()
            );

            case NOT_READY -> {
                stockRedisService.rebuildStock(message.getScreeningId(), cnt);

                log.warn(
                        "[StockRelease] redis key missing, rebuild screeningId={} cnt={} ttl={}m",
                        message.getScreeningId(),
                        cnt,
                        RedisKeyConstants.STOCK_RECOVER_REBUILD_TTL.toMinutes()
                );
            }

            default -> log.warn(
                    "[StockRelease] redis incr abnormal screeningId={}, result={}, left={}",
                    message.getScreeningId(), r.code(), r.left()
            );
        }
    }
}
