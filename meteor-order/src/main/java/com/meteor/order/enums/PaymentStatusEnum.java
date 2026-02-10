package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  支付状态枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10 10:39
 */
@Getter
@AllArgsConstructor
public enum PaymentStatusEnum {

    INIT(0, "初始化"),
    SUCCESS(1, "支付成功"),
    FAIL(2, "支付失败"),
    CLOSED(3, "已关闭");

    @EnumValue
    private final int code;
    private final String desc;
}

