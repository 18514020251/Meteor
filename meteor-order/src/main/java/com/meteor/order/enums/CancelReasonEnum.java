package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 取消原因枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-10
 */
@Getter
@AllArgsConstructor
public enum CancelReasonEnum {

    USER_CANCEL(1, "用户取消"),
    TIMEOUT(2, "超时关闭"),
    SYSTEM_CANCEL(3, "系统取消"),
    OTHER(4, "其他原因");

    @EnumValue
    private final int code;
    private final String desc;
}