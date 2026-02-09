package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  订单状态枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:13
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    WAIT_PAY(0, "待支付"),
    PAID(1, "已支付"),
    CANCELED(2, "已取消"),
    CLOSED_TIMEOUT(3, "超时关闭"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "已退款");

    @EnumValue
    private final int code;
    private final String desc;
}
