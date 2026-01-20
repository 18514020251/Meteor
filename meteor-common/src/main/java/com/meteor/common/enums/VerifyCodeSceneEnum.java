package com.meteor.common.enums;

import lombok.Getter;

/**
 *  验证码场景枚举
 *
 * @author Programmer
 * @date 2026-01-20 17:28
 */
@Getter
public enum VerifyCodeSceneEnum {

    BIND_PHONE("bind_phone", "绑定手机号"),
    RESET_PASSWORD("reset_password", "重置密码"),
    LOGIN("login", "验证码登录");

    private final String code;
    private final String desc;

    VerifyCodeSceneEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}

