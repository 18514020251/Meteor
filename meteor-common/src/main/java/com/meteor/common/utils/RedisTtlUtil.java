package com.meteor.common.utils;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RedisTTL 工具类
 *
 * @author Programmer
 * @date 2026-01-17 20:08
 */
public class RedisTtlUtil {

    /**
     * 基础 TTL（秒）
     */
    public static long toSeconds(Duration duration) {
        return duration.getSeconds();
    }

    /**
     * 带随机抖动的 TTL（防止缓存雪崩）
     *
     * @param base 基础 TTL
     * @param randomBound 随机秒数上限
     */
    public static long withRandom(Duration base, long randomBound) {
        long baseSeconds = base.getSeconds();
        long random = ThreadLocalRandom.current().nextLong(randomBound);
        return baseSeconds + random;
    }
}
