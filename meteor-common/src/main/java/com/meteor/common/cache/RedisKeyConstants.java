package com.meteor.common.cache;

import com.meteor.common.enums.VerifyCodeSceneEnum;

import java.time.Duration;

/**
 * Redis 常量
 *
 * @author Programmer
 * @date 2026-01-17 19:19
 */
public class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    // 用户模块
    private static final String USER_INFO_KEY = "user:info:%d";
    private static final String PHONE_CODE_PREFIX = "sms:code:%s:%s";
    private static final String PHONE_CODE_LIMIT_PREFIX = "sms:limit:%s:%s";
    private static final String PHONE_CODE_IP_LIMIT_KEY = "sms:ip:limit:%s:%s";
    private static final String USER_ROLE_KEY = "user:role:%s";

    public static final Duration USER_INFO_TTL = Duration.ofHours(1);
    public static final Duration USER_INFO_NULL_TTL = Duration.ofSeconds(60);
    public static final Duration PHONE_CODE_LIMIT_TTL = Duration.ofSeconds(60);
    public static final Duration PHONE_CODE_IP_LIMIT_TTL = Duration.ofSeconds(60);
    public static final Duration USER_ROLE_TTL = Duration.ofDays(1);

    public static final String CACHE_NULL_VALUE = "__NULL__";

    public static final String LIMIT_FLAG = "1";
    public static final int PHONE_CODE_IP_LIMIT_COUNT = 5;


    /*
    *  用户信息缓存 TTL 随机抖动上限（秒）
    * */
    public static final long USER_INFO_TTL_RANDOM = 20L;

    public static String phoneCodeKey(VerifyCodeSceneEnum scene, String phone) {
        return String.format(PHONE_CODE_PREFIX, scene.name(), phone);
    }

    public static String phoneCodeLimitKey(VerifyCodeSceneEnum scene, String phone) {
        return String.format(PHONE_CODE_LIMIT_PREFIX, scene.name(), phone);
    }

    public static String phoneCodeIpLimitKey(VerifyCodeSceneEnum scene, String ip) {
        return String.format(PHONE_CODE_IP_LIMIT_KEY, scene.name(), ip);
    }

    public static String buildUserInfoKey(Long userId) {
        return String.format(USER_INFO_KEY, userId);
    }

    public static String buildUserRoleKey(Long userId){
        return String.format(USER_ROLE_KEY, userId);
    }

    public static String buildUserRoleKey(String userId){
        return String.format(USER_ROLE_KEY, userId);
    }

}
