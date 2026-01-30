package com.meteor.common.enums.message;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 *  消息已读状态枚举
 *
 * @author Programmer
 * @date 2026-01-30 19:21
 */
@Getter
public enum MessageReadStatusEnum {

    UNREAD(0, "未读"),
    READ(1, "已读");

    @EnumValue
    private final Integer code;

    private final String desc;

    MessageReadStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
