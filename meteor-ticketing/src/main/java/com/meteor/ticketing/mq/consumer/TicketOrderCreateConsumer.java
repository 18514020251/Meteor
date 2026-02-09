package com.meteor.ticketing.mq.consumer;

import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketOrderCreateMessage;
import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.mapper.TicketMqConsumeLogMapper;
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

    private final ScreeningMapper screeningMapper;
    private final TicketMqConsumeLogMapper consumeLogMapper;

    private static final String TOPIC = "ticket.order.create";

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
                    TOPIC,
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            return;
        }

        int updated = screeningMapper.decrStockAndIncrSold(message.getScreeningId());
        if (updated == 0) {
            throw new IllegalStateException(
                    "screening stock update failed, screeningId=" + message.getScreeningId()
            );
        }

        screeningMapper.markSoldOutIfNeeded(message.getScreeningId());
    }
}