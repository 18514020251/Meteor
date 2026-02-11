package com.meteor.mq.contract.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付成功事件（统计：成交额 + 成交订单数）
 *
 * @author Programmer
 * @date 2026-02-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaySuccessMessage implements Serializable {

    /**
     * 事件唯一ID（雪花ID）
     * 用于 op_analytics_event_log 幂等去重
     */
    private String eventId;

    /**
     * 订单号（用于成交订单去重）
     */
    private String orderNo;

    /**
     * 成交金额（分）—— 用于 GMV 累加
     */
    private Long payAmountCent;

    /**
     * 支付成功时间
     */
    private LocalDateTime payTime;
}
