package com.meteor.ticketing.controller.vo;

import java.time.LocalDateTime;

/**
 *  影片排期信息VO
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 18:33
 */
public record ScreeningVO(
        String screeningId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime saleStartTime,
        LocalDateTime saleEndTime,
        Integer minPrice,
        Integer maxPrice,
        Integer totalTickets,
        Integer availableTickets,
        Integer soldTickets,
        String canBuy,   // 售卖状态
        Long remainSeconds,   // 距开售剩余秒
        Long serverTime   // 服务端时间戳
) {
}
