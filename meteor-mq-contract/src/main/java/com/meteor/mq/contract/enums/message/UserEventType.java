package com.meteor.mq.contract.enums.message;

import lombok.Getter;

/**
 *  用户事件枚举
 *
 * @author Programmer
 * @date 2026-01-29 22:05
 */
@Getter
public enum UserEventType {
    USER_PASSWORD_CHANGED(0),
    MERCHANT_APPLY_SUBMITTED(1),
    MERCHANT_APPLY_REVIEWED(2),
    MERCHANT_APPLY_REJECTED(3);

    private final int code;

    UserEventType(int code) {
        this.code = code;
    }

    public static UserEventType fromCode(int code) {
        for (UserEventType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}

