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
import java.time.ZoneId;
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

    //@Scheduled(fixedDelay = 5_000L)
    @Scheduled(fixedDelay = 5_000L)
    //@Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void warmup() {
        log.info("[ScreeningStockWarmupJob] start");

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
            log.info("[v] id={}", screeningId);
            tryWarmOne(screeningId, now);
        }
    }

    private void tryWarmOne(Long screeningId, LocalDateTime now) {

        String stockKey = buildScreeningStockKey(screeningId);
        String readyKey = buildScreeningStockReadyKey(screeningId);
        String lockKey  = buildScreeningStockWarmLockKey(screeningId);

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(readyKey))) {
            log.info("存在锁，跳过");
            return;
        }

        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "READY", RedisKeyConstants.SCREENING_STOCK_WARM_LOCK_TTL);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("[WarmupLocked] id={} lockKey={}", screeningId, lockKey);
            return;
        }

        Screening s = screeningMapper.selectById(screeningId);
        if (s == null || s.getDeleted() == null || DeleteStatus.DELETED.equals(s.getDeleted())) {
            log.info("票务被删除");
            return;
        }

        if (ScreeningStatusEnum.CANCELED.equals(s.getStatus()) || ScreeningStatusEnum.CLOSED.equals(s.getStatus())) {
            log.info("场次已取消");
            return;
        }
        if (s.getStartTime() != null && now.isAfter(s.getStartTime())) {
            log.info("场次已售罄");
            return;
        }
        if (s.getSaleEndTime() != null && now.isAfter(s.getSaleEndTime())) {
            log.info("场次已售罄");
            return;
        }

        Integer available = s.getAvailableTickets();
        if (available == null) {
            log.info("库存为空");
            return;
        }

        LocalDateTime endBase = s.getSaleEndTime() != null ? s.getSaleEndTime() : s.getStartTime();
        if (endBase == null) {
            log.info("场次未开始");
            return;
        }

        long ttlSeconds = Duration.between(now, endBase.plus(RedisKeyConstants.EXTRA_TTL)).getSeconds();
        if (ttlSeconds <= 0) {
            log.info("场次已 结束");
            return;
        }

        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(available), ttlSeconds, TimeUnit.SECONDS);
        long saleStartEpoch = s.getSaleStartTime().atZone(ZoneId.systemDefault()).toEpochSecond();
        stringRedisTemplate.opsForValue().set(readyKey, String.valueOf(saleStartEpoch), ttlSeconds, TimeUnit.SECONDS);

        log.info("[WarmupOK] id={} stockKey={} value={} ttl={}s", screeningId, stockKey, available, ttlSeconds);
    }


}
