package com.meteor.analytics.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 20:08
 */
@Getter
@AllArgsConstructor
public enum SendState {

    WAIT(0, "待补发"),
    SUCCESS(1, "补发成功"),
    FAILED(2, "补发失败"),
    DOING(3, "补发中");

    @EnumValue
    private final int code;

    private final String desc;
}


