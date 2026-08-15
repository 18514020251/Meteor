package com.meteor.ticketing.service.reservation;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.ticketing.enums.ReservationReserveResult;
import com.meteor.ticketing.redis.RedisScripts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  Redis 原子操作门面
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-16
 */
@Service
@RequiredArgsConstructor
public class TicketReservationRedisService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 原子预留库存。
     *
     * @param reservationId Reservation 业务身份
     * @param screeningId 场次 ID
     * @param quantity 预留数量
     */
    public ReservationReserveResult reserve(String reservationId, Long screeningId, int quantity) {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(screeningId);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(reservationId);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(screeningId);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(screeningId);

        Long result = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(screeningId),
                String.valueOf(quantity)
        );

        if (result == null) {
            throw new IllegalStateException("Redis 预留库存返回 null");
        }

        if (result == 1L) {
            return ReservationReserveResult.RESERVED;
        }
        if (result == 2L) {
            return ReservationReserveResult.IDEMPOTENT;
        }
        if (result == -1L) {
            return ReservationReserveResult.SOLD_OUT;
        }
        if (result == -2L) {
            return ReservationReserveResult.INVALID_QUANTITY;
        }
        if (result == -3L) {
            return ReservationReserveResult.NOT_READY;
        }
        if (result == -4L) {
            return ReservationReserveResult.NOT_STARTED;
        }
        if (result == -5L) {
            return ReservationReserveResult.SALE_CLOSED;
        }

        throw new IllegalStateException("未知的预留库存返回结果：" + result);
    }
}
