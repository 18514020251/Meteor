package com.meteor.ticketing.service.cache.impl;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.ticketing.enums.RedisStockResultEnum;
import com.meteor.ticketing.redis.RedisScripts;
import com.meteor.ticketing.service.cache.ITicketingStockRedisService;
import com.meteor.ticketing.service.cache.model.RedisStockOpResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.meteor.common.cache.RedisKeyConstants.buildScreeningStockKey;
import static com.meteor.common.cache.RedisKeyConstants.buildScreeningStockReadyKey;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 21:56
 */
@Service
@RequiredArgsConstructor
public class TicketingStockRedisServiceImpl implements ITicketingStockRedisService {
    private final StringRedisTemplate redis;

    @Override
    public boolean isSaleStarted(Long screeningId) {
        String readyKey = buildScreeningStockReadyKey(screeningId);
        String v = redis.opsForValue().get(readyKey);
        if (v == null) return false;

        long saleStartEpoch;
        try {
            saleStartEpoch = Long.parseLong(v);
        } catch (Exception e) {
            return false;
        }

        return Instant.now().getEpochSecond() >= saleStartEpoch;
    }

    @Override
    public RedisStockOpResult decrStock1(Long screeningId) {
        String stockKey = buildScreeningStockKey(screeningId);
        Long left = redis.execute(RedisScripts.DECR_STOCK_1, List.of(stockKey));
        RedisStockResultEnum code = RedisStockResultEnum.fromDecrResult(left);
        return new RedisStockOpResult(code, left);
    }

    @Override
    public RedisStockOpResult incrStockN(Long screeningId, int cnt) {
        if (cnt <= 0) {
            return new RedisStockOpResult(RedisStockResultEnum.ERROR, null);
        }

        String stockKey = buildScreeningStockKey(screeningId);
        Long left = redis.execute(
                RedisScripts.INCR_STOCK_N,
                List.of(stockKey),
                String.valueOf(cnt)
        );

        RedisStockResultEnum code = RedisStockResultEnum.fromIncrResult(left);
        return new RedisStockOpResult(code, left);
    }

    @Override
    public void rebuildStock(Long screeningId, int cnt) {
        String stockKey = buildScreeningStockKey(screeningId);
        redis.opsForValue().set(
                stockKey,
                String.valueOf(cnt),
                RedisKeyConstants.STOCK_RECOVER_REBUILD_TTL
        );
    }
}
