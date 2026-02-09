package com.meteor.order.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  操作员类型枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 22:15
 */
@Getter
@AllArgsConstructor
public enum OperatorTypeEnum {

    USER(1),
    SYSTEM(2),
    ADMIN(3);

    @EnumValue
    private final int code;
}
