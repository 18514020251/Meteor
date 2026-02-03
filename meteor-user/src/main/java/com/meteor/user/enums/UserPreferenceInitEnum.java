package com.meteor.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户偏好初始化状态枚举
 *
 * @author Programmer
 */
@Getter
@AllArgsConstructor
public enum UserPreferenceInitEnum {

    /**
     * 未初始化
     */
    NOT_INIT(0),

    /**
     * 已初始化（正常用户）
     */
    INITED(1);

    @EnumValue
    private final int code;

    public static UserPreferenceInitEnum of(Integer code) {
        if (code == null) {
            return NOT_INIT;
        }
        for (UserPreferenceInitEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown UserPreferenceInitEnum code: " + code);
    }
}
