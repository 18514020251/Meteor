package com.meteor.order.controller.vo.pay;

import com.meteor.order.enums.OrderStatusEnum;

import java.time.LocalDateTime;

/**
 *  订单列表项
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 20:39
 */
public record OrderListItemVO(
        String orderNo,
        OrderStatusEnum status,
        Integer payAmount,
        LocalDateTime expireTime,
        LocalDateTime createTime
) {}
