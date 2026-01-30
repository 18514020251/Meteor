package com.meteor.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
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
    //目前没有实现，只做预留
    LOGIN("login", "验证码登录");

    @EnumValue
    private final String code;
    private final String desc;

    VerifyCodeSceneEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}

