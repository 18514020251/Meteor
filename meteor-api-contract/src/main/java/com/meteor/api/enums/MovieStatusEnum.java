package com.meteor.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  电影状态枚举
 *
 * @author Programmer
 * @date 2026-02-06 16:57
 */
@Getter
@AllArgsConstructor
public enum MovieStatusEnum {

    COMING(1, "待映"),
    SHOWING(2, "热映"),
    OFF(3, "下映");

    @EnumValue
    private final Integer code;
    private final String desc;

    public static MovieStatusEnum fromCode(Integer code) {
        for (MovieStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid movie status code: " + code);
    }
}

