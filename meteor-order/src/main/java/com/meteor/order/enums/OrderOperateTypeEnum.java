package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  订单操作类型枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:15
 */
@Getter
@AllArgsConstructor
public enum OrderOperateTypeEnum {

    CREATE(1, "创建订单"),
    PAY_SUCCESS(2, "支付成功"),
    CANCEL(3, "用户取消"),
    CLOSE_TIMEOUT(4, "超时关闭"),
    REFUND(5, "退款");

    @EnumValue
    private final int code;
    private final String desc;
}
