package com.meteor.user.enums;

import lombok.Getter;

/**
 * 用户-状态枚举类
 *
 * @author Programmer
 * @date 2026-01-13 18:49
 */
@Getter
public enum UserStatus {
    NORMAL(0, "正常"),
    DISABLED(1, "禁用");

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus getByCode(Integer code) {
        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
