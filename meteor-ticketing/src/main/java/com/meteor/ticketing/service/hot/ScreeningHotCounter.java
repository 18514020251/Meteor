package com.meteor.ticketing.service.hot;

import com.meteor.common.cache.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 *  影片热度计数器
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 12:09
 */
@Component
@RequiredArgsConstructor
public class ScreeningHotCounter {

    private final StringRedisTemplate redisTemplate;
    private static final Integer ORDER_HOT = 50;
    private static final Integer VIEW_HOT = 1;

    public void onView(Long screeningId) {
        increment(screeningId, ORDER_HOT);
    }

    public void onOrder(Long screeningId) {
        increment(screeningId, VIEW_HOT);
    }

    private void increment(Long screeningId, int delta) {
        redisTemplate.opsForValue()
                .increment(RedisKeyConstants.buildScreeningHotKey(screeningId), delta);
    }
}

