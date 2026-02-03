package com.meteor.common.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Programmer
 * @date 2026-02-03 21:06
 */
@Getter
@AllArgsConstructor
public enum UserPreferenceSourceEnum {
    MANUAL(1),
    PURCHASE(2),
    BROWSE(3);

    private final int code;
}
