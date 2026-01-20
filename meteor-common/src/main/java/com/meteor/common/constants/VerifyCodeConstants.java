package com.meteor.common.constants;

import java.time.Duration;

/**
 *  验证码常量
 *
 * @author Programmer
 * @date 2026-01-20 17:08
 */
public class VerifyCodeConstants {

    private VerifyCodeConstants() {}

    // 验证码 TTL
    public static final Duration PHONE_CODE_TTL = Duration.ofMinutes(5);

    public static final int MIN_CODE = 100000;
    public static final int MAX_CODE = 1000000;


}

