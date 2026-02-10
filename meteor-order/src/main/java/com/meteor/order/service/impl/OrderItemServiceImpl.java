package com.meteor.order.service.impl;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.order.domain.entity.OrderItem;
import com.meteor.order.enums.OrderStatusEnum;
import com.meteor.order.mapper.OrderItemMapper;
import com.meteor.order.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 订单明细表(按张数) 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl
        extends ServiceImpl<OrderItemMapper, OrderItem>
        implements IOrderItemService {

    @Override
    public OrderItem getOneByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return null;
        }

        return lambdaQuery()
                .select(OrderItem::getScreeningId, OrderItem::getTicketCount)
                .eq(OrderItem::getOrderNo, orderNo)
                .eq(OrderItem::getDeleted, DeleteStatus.NORMAL)
                .one();
    }


    @Override
    public boolean closeTimeoutItems(String orderNo, Long operatorId, LocalDateTime now) {

        return lambdaUpdate()
                .set(OrderItem::getStatus, OrderStatusEnum.CLOSED_TIMEOUT)
                .set(OrderItem::getUpdateTime, now)
                .set(OrderItem::getUpdateBy, operatorId)
                .eq(OrderItem::getOrderNo, orderNo)
                .eq(OrderItem::getDeleted, DeleteStatus.NORMAL)
                .eq(OrderItem::getStatus, OrderStatusEnum.WAIT_PAY)
                .update();
    }
}
