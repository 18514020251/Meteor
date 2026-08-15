package com.meteor.ticketing.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.ticketing.domain.entity.Screening;
import com.meteor.ticketing.mapper.ScreeningMapper;
import com.meteor.ticketing.service.warmup.ScreeningStockWarmupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场次库存预热（开售前5分钟写入Redis）
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
    private final ScreeningStockWarmupService warmupService;

    @Scheduled(fixedDelay = 5_000L)
    public void warmup() {
        log.info("[ScreeningStockWarmupJob] start");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime to = now.plus(RedisKeyConstants.WARMUP_WINDOW);

        List<Long> ids = screeningMapper
                .selectList(
                        new LambdaQueryWrapper<Screening>()
                                .select(Screening::getId)
                                .eq(Screening::getDeleted, DeleteStatus.NORMAL)
                                .isNotNull(Screening::getSaleStartTime)
                                .ge(Screening::getSaleStartTime, now)
                                .le(Screening::getSaleStartTime, to)
                )
                .stream()
                .map(Screening::getId)
                .toList();

        if (ids.isEmpty()) {
            return;
        }

        for (Long screeningId : ids) {
            log.info("[v] id={}", screeningId);
            warmupService.warmOne(screeningId, now);
        }
    }
}