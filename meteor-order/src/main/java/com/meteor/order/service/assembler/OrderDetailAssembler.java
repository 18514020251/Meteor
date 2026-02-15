package com.meteor.order.service.assembler;

import com.meteor.order.controller.vo.OrderDetailVO;
import com.meteor.order.domain.entity.Order;
import org.springframework.stereotype.Component;

/**
 *  订单详情组装器
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 20:47
 */
@Component
public class OrderDetailAssembler {

    /**
     * 构建订单详情VO
     *
     * @param order 订单实体
     * @param item 订单项
     * @return 订单详情VO
     */
    public OrderDetailVO buildOrderDetail(Order order, OrderDetailVO.Item item) {
        return new OrderDetailVO(
                order.getOrderNo(),
                order.getStatus(),
                order.getBizType(),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getDiscountAmount(),
                order.getExpireTime(),
                order.getPayTime(),
                order.getCloseTime(),
                order.getPayChannel(),
                order.getPayNo(),
                order.getCancelReason(),
                item
        );
    }
}
