package com.meteor.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *  密码工具类
 *
 * @author Programmer
 * @date 2026-01-16 17:42
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER =
            new BCryptPasswordEncoder();

    /**
     * 明文密码加密
     */
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 密码校验
     *
     * @param rawPassword 明文密码
     * @param encodedPassword   加密后的密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
