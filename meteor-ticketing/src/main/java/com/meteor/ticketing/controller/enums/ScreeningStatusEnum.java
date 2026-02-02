package com.meteor.ticketing.controller.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 *  场次状态枚举
 *
 * @author Programmer
 * @date 2026-02-02 11:33
 */
@Getter
@Schema(description = "场次状态")
public enum ScreeningStatusEnum {

    SCHEDULED(1, "未开售"),
    SELLING(2, "售卖中"),
    SOLD_OUT(3, "已售罄"),
    CLOSED(4, "已停售"),
    CANCELED(5, "已取消");

    @EnumValue
    private final int code;

    private final String desc;

    ScreeningStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}