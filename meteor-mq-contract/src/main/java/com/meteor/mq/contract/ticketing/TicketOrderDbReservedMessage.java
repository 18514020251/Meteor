package com.meteor.mq.contract.ticketing;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DB库存已锁定 → 通知订单模块创建订单
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:42
 */
@Data
public class TicketOrderDbReservedMessage implements Serializable {

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 商家ID */
    private Long merchantId;

    /** 场次ID */
    private Long screeningId;

    /** 电影ID */
    private Long movieId;

    /** 单张票价（分） */
    private Integer unitPrice;

    /** 订单展示快照（JSON字符串） */
    private String snapshotJson;

    /** 创建时间 */
    private LocalDateTime createTime;
}
