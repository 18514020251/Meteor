package com.meteor.api.enums;

import lombok.Getter;

/**
 *  抢票结果枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 11:30
 */
@Getter
public enum GrabOrderResultEnum {

    SUCCESS(0, "下单受理成功"),
    SOLD_OUT(1, "库存不足"),
    NOT_READY(2, "未到开售或库存未预热"),
    FAIL(9, "系统繁忙，请稍后重试");

    private final int code;
    private final String msg;

    GrabOrderResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
