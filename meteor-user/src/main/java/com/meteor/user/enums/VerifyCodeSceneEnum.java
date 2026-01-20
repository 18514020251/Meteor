package com.meteor.user.enums;

import lombok.Getter;

/**
 * @author Programmer
 * @date 2026-01-20 21:51
 */
@Getter
public enum VerifyCodeSceneEnum {

    BIND_PHONE("bind_phone", "绑定手机号"),
    RESET_PASSWORD("reset_pwd", "重置密码");

    private final String code;
    private final String desc;

    VerifyCodeSceneEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
