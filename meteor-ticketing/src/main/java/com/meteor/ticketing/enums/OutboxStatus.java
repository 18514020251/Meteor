package com.meteor.ticketing.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/**
 *  MQ 状态枚举
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 13:37
 */
@Getter
public enum OutboxStatus implements IEnum<Integer> {
    NEW(0, "NEW"),
    SENT(1, "SENT"),
    FAIL(2, "FAIL"),
    DEAD(3, "DEAD"),
    EXPIRED(4, "EXPIRED");

    private final Integer value;
    private final String desc;

    OutboxStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
