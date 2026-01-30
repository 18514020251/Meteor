package com.meteor.common.enums.message;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 *  消息发送状态枚举
 *
 * @author Programmer
 * @date 2026-01-28 22:57
 */
@Getter
public enum MessageSendStatusEnum {

    UNSENT(0, "未发送"),
    SENT(1, "已发送");

    @EnumValue
    private final Integer code;
    private final String desc;

    MessageSendStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
