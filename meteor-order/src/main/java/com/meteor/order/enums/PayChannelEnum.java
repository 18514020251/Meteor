package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  支付渠道枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:14
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    NONE(0, "未支付"),
    ALIPAY(1, "支付宝"),
    WECHAT(2, "微信");

    @EnumValue
    private final int code;
    private final String desc;
}
