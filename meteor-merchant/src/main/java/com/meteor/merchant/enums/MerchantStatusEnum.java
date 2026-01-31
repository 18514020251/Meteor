package com.meteor.merchant.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  商家状态枚举
 *
 * @author Programmer
 * @date 2026-01-31 16:49
 */
@Getter
@AllArgsConstructor
public enum MerchantStatusEnum implements IEnum<Integer> {

    NORMAL(0, "正常"),
    FROZEN(1, "冻结"),
    CLOSED(2, "关闭");

    @EnumValue
    private final Integer value;

    private final String desc;

    @Override
    public Integer getValue() {
        return value;
    }
}
