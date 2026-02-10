package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import jakarta.validation.constraints.NotNull;
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

    public static PayChannelEnum of(@NotNull(message = "channel不能为空") Integer channel) {
        for (PayChannelEnum value : values()) {
            if (value.code == channel) {
                return value;
            }
        }
        throw new BizException(CommonErrorCode.PARAM_INVALID, "invalid pay channel");
    }
}
