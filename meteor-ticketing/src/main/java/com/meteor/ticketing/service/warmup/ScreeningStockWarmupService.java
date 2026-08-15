package com.meteor.ticketing.service.warmup;

import com.meteor.api.enums.ScreeningStatusEnum;
import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.redis.RedisScripts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.meteor.common.cache.RedisKeyConstants.*;

/**
 * 场次库存预热服务
 * 在开售前将库存信息提前写入 Redis，并初始化信号量控制
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningStockWarmupService {

    private final ScreeningMapper screeningMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final int MAX_INFLIGHT = 500;

    /**
     * 预热单个场次
     *
     * @param screeningId 场次ID
     * @param now         当前时间
     */
    public void warmOne(Long screeningId, LocalDateTime now) {
        String stockKey = buildScreeningStockKey(screeningId);
        String readyKey = buildScreeningStockReadyKey(screeningId);
        String lockKey = buildScreeningStockWarmLockKey(screeningId);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(readyKey))) {
            log.info("[WarmupSkip] id={} 已预热", screeningId);
            return;
        }

        String lockToken = UUID.randomUUID().toString();

        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, RedisKeyConstants.SCREENING_STOCK_WARM_LOCK_TTL);

        if (!Boolean.TRUE.equals(locked)) {
            log.info("[WarmupLocked] id={} lockKey={}", screeningId, lockKey);
            return;
        }

        try {
            Screening screening = screeningMapper.selectById(screeningId);
            if (screening == null || DeleteStatus.DELETED.equals(screening.getDeleted())) {
                log.info("[WarmupSkip] id={} 场次不存在或已删除", screeningId);
                return;
            }

            ScreeningStatusEnum status = screening.getStatus();
            if (ScreeningStatusEnum.CANCELED.equals(status) || ScreeningStatusEnum.CLOSED.equals(status)) {
                log.info("[WarmupSkip] id={} 场次已取消或关闭", screeningId);
                return;
            }

            if (screening.getStartTime() != null && now.isAfter(screening.getStartTime())) {
                log.info("[WarmupSkip] id={} 场次已开始", screeningId);
                return;
            }
            if (screening.getSaleEndTime() != null && now.isAfter(screening.getSaleEndTime())) {
                log.info("[WarmupSkip] id={} 售票已结束", screeningId);
                return;
            }

            Integer available = screening.getAvailableTickets();
            if (available == null || available <= 0) {
                log.info("[WarmupSkip] id={} 库存为空或无余票", screeningId);
                return;
            }

            LocalDateTime endBase = screening.getSaleEndTime() != null
                    ? screening.getSaleEndTime()
                    : screening.getStartTime();
            if (endBase == null) {
                log.info("[WarmupSkip] id={} 无有效结束时间", screeningId);
                return;
            }

            long ttlSeconds = Duration.between(now, endBase.plus(RedisKeyConstants.EXTRA_TTL)).getSeconds();
            if (ttlSeconds <= 0) {
                log.info("[WarmupSkip] id={} TTL 已过期", screeningId);
                return;
            }
            String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(screeningId);

            long saleEndEpoch = endBase.atZone(ZoneId.systemDefault()).toEpochSecond();

            stringRedisTemplate.opsForValue().set(
                    saleEndKey,
                    String.valueOf(saleEndEpoch),
                    ttlSeconds,
                    TimeUnit.SECONDS
            );

            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(available), ttlSeconds, TimeUnit.SECONDS);

            long saleStartEpoch = screening.getSaleStartTime()
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();
            stringRedisTemplate.opsForValue().set(readyKey, String.valueOf(saleStartEpoch), ttlSeconds, TimeUnit.SECONDS);

            String semMaxKey = RedisKeyConstants.buildGrabSemMaxKey(screeningId);
            String semPermitsKey = RedisKeyConstants.buildGrabSemPermitsKey(screeningId);
            String semLeaseKey = RedisKeyConstants.buildGrabSemLeaseZsetKey(screeningId);

            stringRedisTemplate.opsForValue().set(semMaxKey, String.valueOf(MAX_INFLIGHT), ttlSeconds, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set(semPermitsKey, String.valueOf(MAX_INFLIGHT), ttlSeconds, TimeUnit.SECONDS);
            stringRedisTemplate.delete(semLeaseKey);

            long endEpoch = endBase.atZone(ZoneId.systemDefault()).toEpochSecond();
            String activeZsetKey = RedisKeyConstants.grabActiveScreeningZsetKey();
            stringRedisTemplate.opsForZSet().add(activeZsetKey, String.valueOf(screeningId), endEpoch);
            stringRedisTemplate.expire(activeZsetKey, Duration.ofDays(1));

            log.info("[WarmupOK] id={} stockKey={} value={} ttl={}s",
                    screeningId, stockKey, available, ttlSeconds);
        } finally {
            releaseWarmupLock(lockKey, lockToken);
        }
    }
    private void releaseWarmupLock(String lockKey, String lockToken) {
        try {
            stringRedisTemplate.execute(RedisScripts.RELEASE_LOCK_IF_OWNER, List.of(lockKey), lockToken);
        } catch (Exception e) {
            log.warn("[WarmupUnlockFailed] lockKey={}", lockKey, e);
        }
    }
}