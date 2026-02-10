package com.meteor.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.order.domain.entity.Order;
import com.meteor.order.enums.CancelReasonEnum;
import com.meteor.order.enums.OrderStatusEnum;
import com.meteor.order.mapper.OrderMapper;
import com.meteor.order.service.IOrderCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:31
 */
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl
        extends ServiceImpl<OrderMapper, Order>
        implements IOrderCommandService {

    @Override
    public boolean existsByOrderNo(String orderNo) {
        return lambdaQuery()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getDeleted, DeleteStatus.NORMAL)
                .exists();
    }

    @Override
    public boolean closeTimeout(String orderNo, LocalDateTime now) {
        return lambdaUpdate()
                .set(Order::getStatus, OrderStatusEnum.CLOSED_TIMEOUT)
                .set(Order::getCloseTime, now)
                .set(Order::getCancelReason, CancelReasonEnum.TIMEOUT)
                .set(Order::getUpdateTime, now)
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getStatus, OrderStatusEnum.WAIT_PAY)
                .eq(Order::getDeleted, DeleteStatus.NORMAL)
                .update();
    }

}

