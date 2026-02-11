package com.meteor.mq.contract.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付单创建事件（统计：支付尝试数 +1）
 *
 * @author Programmer
 * @date 2026-02-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayCreatedMessage implements Serializable {

    /**
     * 事件唯一ID（雪花ID）
     * 用于 op_analytics_event_log 幂等去重
     */
    private String eventId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 事件发生时间
     */
    private LocalDateTime occurTime;
}
