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

    USER_PASSWORD_CHANGED,
    MERCHANT_APPLY_SUBMITTED,
    MERCHANT_APPLY_REVIEWED;

}

