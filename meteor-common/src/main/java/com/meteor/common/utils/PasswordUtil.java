package com.meteor.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
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
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
