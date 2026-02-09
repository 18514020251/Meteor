package com.meteor.order.service.impl;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.order.OrderPayTimeoutMessage;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.order.domain.entity.Order;
import com.meteor.order.mapper.OrderItemMapper;
import com.meteor.order.mapper.OrderMapper;
import com.meteor.order.mapper.OrderOperateLogMapper;
import com.meteor.order.mq.publisher.OrderPayTimeoutPublisher;
import com.meteor.order.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.order.service.assembler.OrderCreateAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@RequiredArgsConstructor
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderItemMapper orderItemMapper;
    private final OrderOperateLogMapper operateLogMapper;
    private final OrderCreateAssembler assembler;
    private final SnowflakeIdGenerator snowflake;
    private final OrderPayTimeoutPublisher orderPayTimeoutPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderFromTicket(TicketOrderDbReservedMessage msg) {

        // 幂等：订单号存在直接返回
        boolean exists = lambdaQuery()
                .eq(Order::getOrderNo, msg.getOrderNo())
                .eq(Order::getDeleted, DeleteStatus.NORMAL)
                .exists();
        if (exists) {
            return;
        }

        long orderId = snowflake.nextId();
        LocalDateTime now = LocalDateTime.now();

        var order = assembler.buildOrder(msg, orderId, now);
        var item  = assembler.buildItem(msg, orderId, now);
        var log   = assembler.buildCreateLog(orderId, msg.getOrderNo(), now);

        baseMapper.insert(order);
        orderItemMapper.insert(item);
        operateLogMapper.insert(log);

        OrderPayTimeoutMessage timeoutMsg = new OrderPayTimeoutMessage(order.getOrderNo(), now);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderPayTimeoutPublisher.publishOrThrow(timeoutMsg);
            }
        });
    }
}

