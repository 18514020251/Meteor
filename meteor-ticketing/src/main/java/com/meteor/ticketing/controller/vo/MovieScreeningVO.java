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
        String screeningId,        // 场次ID
        LocalDateTime startTime,   // 开始时间
        LocalDateTime saleStartTime, // 开售时间
        SaleModeEnum saleMode,     // 销售模式
        Integer basePrice,         // 基础价格（分）

        SaleStateEnum canBuy,            // 是否可以购买
        Long remainSeconds         // 距离开售剩余秒数
) {}
