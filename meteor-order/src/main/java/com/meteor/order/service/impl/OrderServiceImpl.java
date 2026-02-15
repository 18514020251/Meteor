package com.meteor.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.mq.contract.order.OrderPayTimeoutMessage;
import com.meteor.mq.contract.ticketing.TicketOrderDbReservedMessage;
import com.meteor.order.controller.vo.OrderDetailVO;
import com.meteor.order.controller.vo.pay.OrderListItemVO;
import com.meteor.order.domain.entity.Order;
import com.meteor.order.domain.entity.OrderItem;
import com.meteor.order.enums.OrderStatusEnum;
import com.meteor.order.mapper.OrderItemMapper;
import com.meteor.order.mapper.OrderMapper;
import com.meteor.order.mapper.OrderOperateLogMapper;
import com.meteor.order.mq.publisher.OrderPayTimeoutPublisher;
import com.meteor.order.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.order.service.assembler.OrderCreateAssembler;
import com.meteor.order.service.assembler.OrderDetailAssembler;
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
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderItemMapper orderItemMapper;
    private final OrderOperateLogMapper operateLogMapper;
    private final OrderCreateAssembler assembler;
    private final OrderDetailAssembler orderDetailAssembler;
    private final SnowflakeIdGenerator snowflake;
    private final OrderPayTimeoutPublisher orderPayTimeoutPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderFromTicket(TicketOrderDbReservedMessage msg) {

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
        var item = assembler.buildItem(msg, orderId, now);
        var log = assembler.buildCreateLog(orderId, msg.getOrderNo(), now);

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

    @Override
    public OrderDetailVO detail(String orderNo, Long userId) {
        Order order = getOrderDetail(orderNo, userId);

        OrderItem it = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderNo, orderNo)
                .eq(OrderItem::getDeleted, DeleteStatus.NORMAL)
                .last("limit 1"));

        OrderDetailVO.Item item = null;
        if (it != null) {
            item = new OrderDetailVO.Item(
                    it.getScreeningId(),
                    it.getMovieId(),
                    it.getMerchantId(),
                    it.getTicketCount(),
                    it.getUnitPrice(),
                    it.getAmount(),
                    it.getSnapshot()
            );
        }

        return orderDetailAssembler.buildOrderDetail(order, item);
    }

    @Override
    public Page<OrderListItemVO> page(Long userId, int page, int size, OrderStatusEnum status) {

        Page<Order> p = new Page<>(page, size);

        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getDeleted, DeleteStatus.NORMAL)
                .orderByDesc(Order::getCreateTime);

        if (status != null) {
            qw.eq(Order::getStatus, status);
        }

        Page<Order> result = baseMapper.selectPage(p, qw);

        Page<OrderListItemVO> out = new Page<>(page, size, result.getTotal());

        out.setRecords(result.getRecords().stream()
                .map(o -> new OrderListItemVO(
                        o.getOrderNo(),
                        o.getStatus(),
                        o.getPayAmount(),
                        o.getExpireTime(),
                        o.getCreateTime()
                ))
                .toList());

        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String orderNo, Long userId) {

        Order order = getOrderDetail(orderNo, userId);

        // 只允许删除“已结束订单”
        if (order.getStatus() == OrderStatusEnum.WAIT_PAY ||
                order.getStatus() == OrderStatusEnum.PAID ||
                order.getStatus() == OrderStatusEnum.REFUNDING) {

            throw new BizException(CommonErrorCode.PARAM_INVALID, "当前订单状态不可删除");
        }

        baseMapper.update(null, new LambdaUpdateWrapper<Order>()
                .set(Order::getDeleted, DeleteStatus.DELETED)
                .set(Order::getUpdateTime, LocalDateTime.now())
                .set(Order::getUpdateBy, userId)
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)
                .eq(Order::getDeleted, DeleteStatus.NORMAL));
    }

    /**
     * 根据订单号和用户ID获取订单详情
     *
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 订单详情
     * @throws BizException 当订单不存在时抛出
     */
    private Order getOrderDetail(String orderNo, Long userId) {
        Order order = baseMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)
                .eq(Order::getDeleted, DeleteStatus.NORMAL)
                .last("limit 1"));

        if (order == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "订单不存在");
        }

        return order;
    }

}

