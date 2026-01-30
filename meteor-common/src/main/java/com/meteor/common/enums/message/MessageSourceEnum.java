package com.meteor.common.enums.message;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 *  消息来源枚举
 *
 * @author Programmer
 * @date 2026-01-30 19:24
 */
public enum MessageSourceEnum {
    SYSTEM(0 , "系统消息"),
    EVENT(1 , "业务事件");

    @EnumValue
    private final int code;
    private final String desc;

    MessageSourceEnum(int code , String desc) {
        this.code = code;
        this.desc = desc;
    }


}
