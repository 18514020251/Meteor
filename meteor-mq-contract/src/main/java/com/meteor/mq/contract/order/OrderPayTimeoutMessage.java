package com.meteor.mq.contract.order;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单支付超时检查消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 19:16
 */
@Data
@AllArgsConstructor
public class OrderPayTimeoutMessage implements Serializable {

    /** 订单号 */
    private String orderNo;

    /** 创建时间（可选，方便排查） */
    private LocalDateTime createTime;
}
