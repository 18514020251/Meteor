package com.meteor.ticketing.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 *  订单数据库预留消息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 17:53
 */
@Data
public class ScreeningOrderSnapshot {
    private Long id;
    private Long merchantId;
    private Long movieId;
    private Integer basePrice;
    private LocalDateTime startTime;
}
