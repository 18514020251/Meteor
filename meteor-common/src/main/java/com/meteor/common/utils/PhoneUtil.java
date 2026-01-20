package com.meteor.common.utils;

import java.util.concurrent.ThreadLocalRandom;

import static com.meteor.common.constants.VerifyCodeConstants.MAX_CODE;
import static com.meteor.common.constants.VerifyCodeConstants.MIN_CODE;

/**
 *  手机号工具类
 *
 * @author Programmer
 * @date 2026-01-20 17:01
 */
public final class PhoneUtil {

    private PhoneUtil() {}

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    public static boolean isValid(String phone) {
        if (phone == null) {
            return false;
        }
        return phone.matches(PHONE_REGEX);
    }

    public static String generateSixDigit() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(MIN_CODE, MAX_CODE)
        );
    }
}

