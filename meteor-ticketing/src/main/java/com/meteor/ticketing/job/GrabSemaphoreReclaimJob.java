package com.meteor.ticketing.job;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.ticketing.redis.GrabSemaphoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

/**
 *  抢购信号 semaphore 锁的过期回收
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-15 15:26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrabSemaphoreReclaimJob {

    private final StringRedisTemplate stringRedisTemplate;
    private final GrabSemaphoreService grabSemaphoreService;

    @Scheduled(fixedDelay = 500L)
    public void reclaim() {

        long nowEpoch = Instant.now().getEpochSecond();
        String zsetKey = RedisKeyConstants.grabActiveScreeningZsetKey();

        Set<String> ids = stringRedisTemplate.opsForZSet()
                .rangeByScore(zsetKey, nowEpoch, Double.POSITIVE_INFINITY);

        if (ids == null || ids.isEmpty()) {
            return;
        }

        int batch = 200;
        for (String idStr : ids) {
            Long screeningId;
            try {
                screeningId = Long.valueOf(idStr);
            }catch (Exception ignore) {
                continue;
            }

            long n = grabSemaphoreService.reclaimExpired(screeningId, batch);
            if (n > 0) {
                log.info("[GrabSemReclaim] screeningId={} reclaimed={}", screeningId, n);
            }
        }
    }
}
