package com.meteor.mq.contract.ticketing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抢票订单创建消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketOrderCreateMessage implements Serializable {

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 场次ID */
    private Long screeningId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
