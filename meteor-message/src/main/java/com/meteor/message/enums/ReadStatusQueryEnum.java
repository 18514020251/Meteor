package com.meteor.message.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 *  通知状态查询枚举
 *
 * @author Programmer
 * @date 2026-01-29 18:08
 */
@Getter
public enum ReadStatusQueryEnum {
    UNREAD(0 , "未读"),
    READ(1 , "已读"),
    ALL(2 , "全部");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;

    ReadStatusQueryEnum(Integer code , String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     *  根据code获取枚举
     * */
    public static ReadStatusQueryEnum fromCode(Integer code) {
        if (code == null) {
            return ALL;
        }
        for (ReadStatusQueryEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ALL;
    }
}
