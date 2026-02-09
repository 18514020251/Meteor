package com.meteor.mq.contract.ticketing;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 释放库存消息（订单超时/取消后）
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 20:23
 */
@Data
public class TicketStockReleaseMessage implements Serializable {

    /** 订单号（幂等键） */
    private String orderNo;

    /** 场次ID */
    private Long screeningId;

    /** 释放张数 */
    private Integer ticketCount;

    /** 发送时间 */
    private LocalDateTime createTime;
}
