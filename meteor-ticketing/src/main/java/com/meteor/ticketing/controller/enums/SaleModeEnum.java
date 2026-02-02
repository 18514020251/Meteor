package com.meteor.ticketing.controller.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 *  场次状态枚举
 *
 * @author Programmer
 * @date 2026-02-02 11:29
 */
@Getter
@Schema(description = "销售模式")
public enum SaleModeEnum {

    AUTO(1, "抢票模式"),
    MANUAL(2, "选座模式"),
    MIXED(3, "混合模式");

    @EnumValue
    private final int code;

    private final String desc;

    SaleModeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
