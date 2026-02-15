package com.meteor.order.controller.vo;

import com.meteor.order.enums.CancelReasonEnum;
import com.meteor.order.enums.OrderBizTypeEnum;
import com.meteor.order.enums.OrderStatusEnum;
import com.meteor.order.enums.PayChannelEnum;

import java.time.LocalDateTime;

/**
 *  订单详情
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 20:03
 */
public record OrderDetailVO(
        String orderNo,
        OrderStatusEnum status,
        OrderBizTypeEnum bizType,

        Integer totalAmount,
        Integer payAmount,
        Integer discountAmount,

        LocalDateTime expireTime,
        LocalDateTime payTime,
        LocalDateTime closeTime,

        PayChannelEnum payChannel,
        String payNo,

        CancelReasonEnum cancelReason,

        Item item
) {
    public record Item(
            Long screeningId,
            Long movieId,
            Long merchantId,
            Integer ticketCount,
            Integer unitPrice,
            Integer amount,
            String snapshot
    ) {}
}