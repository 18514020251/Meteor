package com.meteor.common.cache;

import com.meteor.common.enums.user.VerifyCodeSceneEnum;

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

    // ================== 用户相关 ==================
    private static final String USER_INFO_KEY = "user:info:%d";
    private static final String PHONE_CODE_PREFIX = "sms:code:%s:%s";
    private static final String PHONE_CODE_LIMIT_PREFIX = "sms:limit:%s:%s";
    private static final String PHONE_CODE_IP_LIMIT_KEY = "sms:ip:limit:%s:%s";
    private static final String USER_ROLE_KEY = "user:role:%s";
    private static final String SCREENING_HOT_KEY = "screening:hot:%d";

    private static final String ONLINE_USER_ZSET_KEY = "online:user:zset";

    private static final String ONLINE_USER_DETAIL_KEY = "online:user:detail:%s";

    public static final Duration USER_INFO_TTL = Duration.ofHours(1);
    public static final Duration USER_INFO_NULL_TTL = Duration.ofSeconds(60);
    public static final Duration PHONE_CODE_LIMIT_TTL = Duration.ofSeconds(60);
    public static final Duration PHONE_CODE_IP_LIMIT_TTL = Duration.ofSeconds(60);
    public static final Duration USER_ROLE_TTL = Duration.ofDays(1);

    public static final Duration ONLINE_USER_TTL = Duration.ofHours(24);


    public static final String CACHE_NULL_VALUE = "__NULL__";

    // ================== 订单/抢票库存 ==================
    private static final String SCREENING_STOCK_KEY = "screening:stock:%d";
    private static final String SCREENING_STOCK_READY_KEY = "screening:stock:ready:%d";
    private static final String SCREENING_STOCK_WARM_LOCK_KEY = "lock:screening:stock:warm:%d";

    /**
     * 预热锁 TTL：避免死锁
     */
    public static final Duration SCREENING_STOCK_WARM_LOCK_TTL = Duration.ofSeconds(60);

    public static final Duration WARMUP_WINDOW = Duration.ofMinutes(5);
    public static final Duration EXTRA_TTL = Duration.ofSeconds(300);




    public static final String LIMIT_FLAG = "1";
    public static final int PHONE_CODE_IP_LIMIT_COUNT = 5;


    public static final String ONLINE_USER_FIELD_TOKEN = "token";
    public static final String ONLINE_USER_FIELD_IP = "ip";
    public static final String ONLINE_USER_FIELD_LOGIN_TIME = "loginTime";
    public static final String ONLINE_USER_FIELD_ROLE = "role";


    /**
    *  用户信息缓存 TTL 随机抖动上限（秒）
    */
    public static final long USER_INFO_TTL_RANDOM = 20L;

    public static String phoneCodeKey(VerifyCodeSceneEnum scene, String phone) {
        return String.format(PHONE_CODE_PREFIX, scene.getCode(), phone);
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

    public static String buildUserRoleKey(Object userId){
        return String.format(USER_ROLE_KEY, userId);
    }

    public static String buildScreeningHotKey(Long movieId) {
        return String.format(SCREENING_HOT_KEY, movieId);
    }

    public static String onlineUserZsetKey() {
        return ONLINE_USER_ZSET_KEY;
    }

    public static String buildOnlineUserDetailKey(Object userId) {
        return String.format(ONLINE_USER_DETAIL_KEY, userId);
    }

    public static String buildScreeningStockKey(Long screeningId) {
        return String.format(SCREENING_STOCK_KEY, screeningId);
    }

    public static String buildScreeningStockReadyKey(Long screeningId) {
        return String.format(SCREENING_STOCK_READY_KEY, screeningId);
    }

    public static String buildScreeningStockWarmLockKey(Long screeningId) {
        return String.format(SCREENING_STOCK_WARM_LOCK_KEY, screeningId);
    }
}
