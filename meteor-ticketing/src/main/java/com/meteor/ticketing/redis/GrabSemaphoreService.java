package com.meteor.ticketing.redis;

import com.meteor.common.cache.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 *  抢票信号量服务
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 15:24
 */
@Component
@RequiredArgsConstructor
public class GrabSemaphoreService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     *  尝试获取抢票信号量
     *
     *  @param screeningId 屏幕ID
     *  @param ttlMs 抢票信号量的过期时间
     *  @return 抢票信号量的租约信息
     */
    public Lease tryAcquire(Long screeningId, long ttlMs) {
        String token = UUID.randomUUID().toString();

        List<String> keys = List.of(
                RedisKeyConstants.buildGrabSemPermitsKey(screeningId),
                RedisKeyConstants.buildGrabSemMaxKey(screeningId),
                RedisKeyConstants.buildGrabSemLeaseZsetKey(screeningId)
        );

        Long expireAtMs = stringRedisTemplate.execute(
                RedisScripts.GRAB_SEM_TRY_ACQUIRE,
                keys,
                String.valueOf(ttlMs),
                token
        );

        if (expireAtMs == null || expireAtMs <= 0) {
            return null;
        }
        return new Lease(token, expireAtMs);
    }

    public void release(Long screeningId, String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        List<String> keys = List.of(
                RedisKeyConstants.buildGrabSemPermitsKey(screeningId),
                RedisKeyConstants.buildGrabSemMaxKey(screeningId),
                RedisKeyConstants.buildGrabSemLeaseZsetKey(screeningId)
        );

        stringRedisTemplate.execute(
                RedisScripts.GRAB_SEM_RELEASE,
                keys,
                token
        );
    }

    public long reclaimExpired(Long screeningId, int batchSize) {
        List<String> keys = List.of(
                RedisKeyConstants.buildGrabSemPermitsKey(screeningId),
                RedisKeyConstants.buildGrabSemMaxKey(screeningId),
                RedisKeyConstants.buildGrabSemLeaseZsetKey(screeningId)
        );

        Long n = stringRedisTemplate.execute(
                RedisScripts.GRAB_SEM_RECLAIM_EXPIRED,
                keys,
                String.valueOf(batchSize)
        );
        return n == null ? 0 : n;
    }

    public record Lease(String token, Long expireAtMs) {}
}

