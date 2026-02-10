package com.meteor.order.mq.consumer;

import com.meteor.mq.contract.ticketing.TicketOrderContract;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.order.mapper.OrderMqConsumeLogMapper;
import com.meteor.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 抢票订单创建消费者
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:13
 */
@Component
@RequiredArgsConstructor
public class TicketOrderCreateConsumer {

    private final OrderMqConsumeLogMapper consumeLogMapper;
    private final IOrderService orderCreateService;

    private static final String TOPIC =
            TicketOrderContract.RoutingKey.TICKET_ORDER_CREATE;

    @RabbitListener(
            queues = TicketOrderContract.Queue.TICKET_ORDER_DB_RESERVED,
            errorHandler = "mqRejectErrorHandler"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handle(TicketOrderDbReservedMessage message) {

        if (message == null
                || message.getOrderNo() == null
                || message.getUserId() == null
                || message.getScreeningId() == null) {
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

        orderCreateService.createOrderFromTicket(message);
    }
}
