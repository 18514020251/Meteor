package com.meteor.order.mq.consumer;

import com.meteor.mq.contract.order.OrderPayTimeoutContract;
import com.meteor.mq.contract.order.OrderPayTimeoutMessage;
import com.meteor.mq.contract.ticketing.TicketStockReleaseMessage;
import com.meteor.order.domain.entity.OrderItem;
import com.meteor.order.mq.assembler.TicketStockReleaseMessageAssembler;
import com.meteor.order.mq.publisher.TicketStockReleasePublisher;
import com.meteor.order.service.IOrderCommandService;
import com.meteor.order.service.IOrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单支付超时消费者：15分钟后检查并关单
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 19:26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPayTimeoutConsumer {

    private final IOrderCommandService orderCommandService;
    private final IOrderItemService orderItemService;
    private final TicketStockReleasePublisher stockReleasePublisher;
    private final TicketStockReleaseMessageAssembler assembler;

    @RabbitListener(
            queues = OrderPayTimeoutContract.Queue.ORDER_PAY_TIMEOUT,
            errorHandler = "mqRejectErrorHandler"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handle(OrderPayTimeoutMessage message) {

        if (message == null || message.getOrderNo() == null) {
            return;
        }

        String orderNo = message.getOrderNo();
        LocalDateTime now = LocalDateTime.now();

        boolean closed = orderCommandService.closeTimeout(orderNo, now);
        if (!closed) {
            return;
        }

        log.info("order timeout closed, orderNo={}", orderNo);

        OrderItem item = orderItemService.getOneByOrderNo(orderNo);
        if (item == null) {
            throw new IllegalStateException("order item not found: " + orderNo);
        }

        TicketStockReleaseMessage releaseMsg = assembler.from(orderNo, item, now);
        stockReleasePublisher.publishOrThrow(releaseMsg);
    }
}
