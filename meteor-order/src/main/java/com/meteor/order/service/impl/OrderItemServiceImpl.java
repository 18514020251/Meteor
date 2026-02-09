package com.meteor.order.service.impl;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.order.domain.entity.OrderItem;
import com.meteor.order.mapper.OrderItemMapper;
import com.meteor.order.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return lambdaQuery()
                .select(OrderItem::getScreeningId, OrderItem::getTicketCount)
                .eq(OrderItem::getOrderNo, orderNo)
                .eq(OrderItem::getDeleted, DeleteStatus.NORMAL)
                .last("LIMIT 1")
                .one();
    }
}
