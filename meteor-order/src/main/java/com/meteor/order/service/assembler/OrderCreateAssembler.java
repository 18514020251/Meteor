package com.meteor.order.service.assembler;

import com.meteor.mq.contract.order.OrderPayTimeoutContract;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.order.domain.entity.Order;
import com.meteor.order.domain.entity.OrderItem;
import com.meteor.order.domain.entity.OrderOperateLog;
import com.meteor.order.enums.*;
import com.meteor.order.constants.OrderBizConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MQ → 订单领域对象转换器
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:38
 */
@Component
public class OrderCreateAssembler {

    public Order buildOrder(TicketOrderDbReservedMessage msg, long orderId, LocalDateTime now) {

        int amount = msg.getUnitPrice();

        Order order = new Order();
        order.setId(orderId);
        order.setOrderNo(msg.getOrderNo());
        order.setUserId(msg.getUserId());
        order.setMerchantId(msg.getMerchantId());

        order.setStatus(OrderStatusEnum.WAIT_PAY);
        order.setBizType(OrderBizTypeEnum.MOVIE_TICKET);

        order.setTotalAmount(amount);
        order.setPayAmount(amount);
        order.setDiscountAmount(OrderBizConstants.NO_DISCOUNT);

        order.setExpireTime(now.plus(OrderPayTimeoutContract.PAY_TTL));
        order.setPayChannel(PayChannelEnum.NONE);

        order.setCreateTime(now);
        order.setUpdateTime(now);
        return order;
    }

    public OrderItem buildItem(TicketOrderDbReservedMessage msg, long orderId, LocalDateTime now) {

        int ticketCount = OrderBizConstants.DEFAULT_TICKET_COUNT;
        int amount = msg.getUnitPrice();

        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setOrderNo(msg.getOrderNo());
        item.setScreeningId(msg.getScreeningId());
        item.setMovieId(msg.getMovieId());
        item.setMerchantId(msg.getMerchantId());

        item.setTicketCount(ticketCount);
        item.setUnitPrice(amount);
        item.setAmount(amount);
        item.setSnapshot(msg.getSnapshotJson());

        item.setStatus(OrderStatusEnum.WAIT_PAY);

        item.setCreateTime(now);
        item.setUpdateTime(now);
        return item;
    }

    public OrderOperateLog buildCreateLog(long orderId, String orderNo, LocalDateTime now) {

        OrderOperateLog log = new OrderOperateLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);

        log.setFromStatus(null);
        log.setToStatus(OrderStatusEnum.WAIT_PAY);
        log.setOperateType(OrderOperateTypeEnum.CREATE);
        log.setOperatorType(OperatorTypeEnum.SYSTEM);
        log.setRemark("MQ创建订单");

        log.setCreateTime(now);
        log.setUpdateTime(now);
        return log;
    }
}

