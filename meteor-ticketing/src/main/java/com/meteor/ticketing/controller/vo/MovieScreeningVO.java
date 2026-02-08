package com.meteor.ticketing.controller.vo;

import com.meteor.api.enums.SaleModeEnum;
import com.meteor.ticketing.enums.SaleStateEnum;

import java.time.LocalDateTime;

/**
 *  电影场次信息VO
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 10:51
 */
public record MovieScreeningVO(
        String screeningId,
        LocalDateTime startTime,
        LocalDateTime endTime,

        LocalDateTime saleStartTime,
        LocalDateTime saleEndTime,

        SaleModeEnum saleMode,

        Integer basePrice,
        Integer minPrice,
        Integer maxPrice,

        Integer totalTickets,
        Integer availableTickets,
        Integer soldTickets,

        SaleStateEnum canBuy,
        Long remainSeconds,

        Long serverTime
) {}
