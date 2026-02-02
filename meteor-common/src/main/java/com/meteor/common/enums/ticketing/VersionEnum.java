package com.meteor.common.enums.ticketing;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  版本号枚举类
 *
 * @author Programmer
 * @date 2026-02-02 11:47
 */
@AllArgsConstructor
@Getter
public enum VersionEnum {
    INIT(0),
    V1(1);

    private final int value;

    public int value() {
        return value;
    }
}

