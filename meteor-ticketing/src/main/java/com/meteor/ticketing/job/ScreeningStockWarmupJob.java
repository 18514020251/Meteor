package com.meteor.ticketing.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.api.enums.ScreeningStatusEnum;
import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.mapper.ScreeningMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.meteor.common.cache.RedisKeyConstants.*;

/**
 *  场次库存预热（开售前5分钟写入Redis）
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 8:54
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScreeningStockWarmupJob {
    private final ScreeningMapper screeningMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedDelay = 15_000L)
    public void warmup() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime to = now.plus(RedisKeyConstants.WARMUP_WINDOW);

        List<Long> ids = screeningMapper.selectList(
                new LambdaQueryWrapper<Screening>()
                        .select(Screening::getId)
                        .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                        .isNotNull(Screening::getSaleStartTime)
                        .ge(Screening::getSaleStartTime, now)
                        .le(Screening::getSaleStartTime, to)
        ).stream().map(Screening::getId).toList();

        if (ids.isEmpty()) {
            return;
        }

        for (Long screeningId : ids) {
            tryWarmOne(screeningId, now);
        }
    }

    private void tryWarmOne(Long screeningId, LocalDateTime now) {

        String stockKey = buildScreeningStockKey(screeningId);
        String readyKey = buildScreeningStockReadyKey(screeningId);
        String lockKey  = buildScreeningStockWarmLockKey(screeningId);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(readyKey))) {
            return;
        }

        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "READY", RedisKeyConstants.SCREENING_STOCK_WARM_LOCK_TTL);
        if (!Boolean.TRUE.equals(locked)) {
            return;
        }

        Screening s = screeningMapper.selectById(screeningId);
        if (s == null || s.getDeleted() == null || DeleteStatus.DELETED.equals(s.getDeleted())) {
            return;
        }

        if (ScreeningStatusEnum.CANCELED.equals(s.getStatus()) || ScreeningStatusEnum.CLOSED.equals(s.getStatus())) {
            return;
        }
        if (s.getStartTime() != null && now.isAfter(s.getStartTime())) {
            return;
        }
        if (s.getSaleEndTime() != null && now.isAfter(s.getSaleEndTime())) {
            return;
        }

        Integer available = s.getAvailableTickets();
        if (available == null) {
            return;
        }

        LocalDateTime endBase = s.getSaleEndTime() != null ? s.getSaleEndTime() : s.getStartTime();
        if (endBase == null) {
            return;
        }

        long ttlSeconds = Duration.between(now, endBase.plus(RedisKeyConstants.EXTRA_TTL)).getSeconds();
        if (ttlSeconds <= 0) {
            return;
        }

        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(available), ttlSeconds, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(readyKey, "READY", ttlSeconds, TimeUnit.SECONDS);

        log.info("[WarmupOK] id={} stockKey={} value={} ttl={}s", screeningId, stockKey, available, ttlSeconds);
    }


}
