package com.meteor.ticketing.mq.consumer;

import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.ticketing.controller.dto.ScreeningOrderSnapshot;
import com.meteor.ticketing.mapper.TicketMqConsumeLogMapper;
import com.meteor.ticketing.mq.assmabler.TicketOrderDbReservedMessageAssembler;
import com.meteor.ticketing.mq.publisher.TicketOrderDbReservedPublisher;
import com.meteor.ticketing.service.IScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 *  抢票下单消息消费者
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 15:13
 */
@Component
@RequiredArgsConstructor
public class TicketOrderCreateConsumer {

    private final TicketMqConsumeLogMapper consumeLogMapper;
    private final TicketOrderDbReservedPublisher dbReservedPublisher;
    private final TicketOrderDbReservedMessageAssembler assembler;

    private final IScreeningService screeningService;

    @RabbitListener(
            queues = TicketOrderContract.Queue.TICKET_ORDER_CREATE,
            errorHandler = "mqRejectErrorHandler"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handle(TicketOrderCreateMessage message) {

        if (message == null || message.getOrderNo() == null || message.getScreeningId() == null) {
            return;
        }

        try {
            consumeLogMapper.insert(
                    message.getOrderNo(),
                    TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE,
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            return;
        }

        boolean ok = screeningService.decrStockAndIncrSold(message.getScreeningId());
        if (!ok) {
            throw new IllegalStateException(
                    "screening stock update failed, screeningId=" + message.getScreeningId()
            );
        }

        screeningService.markSoldOutIfNeeded(message.getScreeningId());

        ScreeningOrderSnapshot screening = screeningService.getOrderSnapshot(message.getScreeningId());
        if (screening == null) {
            throw new IllegalStateException("screening not found: " + message.getScreeningId());
        }

        var dbMsg = assembler.from(message, screening);
        dbReservedPublisher.publishOrThrow(dbMsg);
    }
}