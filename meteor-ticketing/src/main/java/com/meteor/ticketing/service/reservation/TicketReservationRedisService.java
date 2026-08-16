package com.meteor.ticketing.service.reservation;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.ticketing.enums.ReservationReserveResult;
import com.meteor.ticketing.enums.ReservationStatus;
import com.meteor.ticketing.enums.ReservationTransitionResult;
import com.meteor.ticketing.redis.RedisScripts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
     * @param screeningId  场次 ID
     * @param quantity     预留数量
     * @return reserve 业务结果
     */
    public ReservationReserveOutcome  reserve(String reservationId, Long screeningId, int quantity) {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(screeningId);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(reservationId);
        String readyKey = RedisKeyConstants.buildScreeningStockReadyKey(screeningId);
        String saleEndKey = RedisKeyConstants.buildScreeningSaleEndKey(screeningId);

        List<?> result = redisTemplate.execute(
                RedisScripts.RESERVE_TICKET,
                List.of(stockKey, reservationKey, readyKey, saleEndKey),
                String.valueOf(screeningId),
                String.valueOf(quantity)
        );

        return convertReserveResult(result);
    }

    /**
     * 正常释放 Reservation，恢复库存。
     *
     * @param reservationId Reservation ID
     * @param screeningId  场次 ID
     */
    public ReservationTransitionResult release(String reservationId, Long screeningId) {
        return releaseReservation(reservationId, screeningId, ReservationStatus.RELEASED);
    }

    /**
     * 补偿释放 Reservation，恢复库存。
     *
     * @param reservationId Reservation ID
     * @param screeningId  场次 ID
     */
    public ReservationTransitionResult compensate(String reservationId, Long screeningId) {
        return releaseReservation(reservationId, screeningId, ReservationStatus.COMPENSATED);
    }

    /**
     * 确认 Reservation，不修改库存。
     *
     * @param reservationId Reservation ID
     */
    public ReservationTransitionResult confirm(String reservationId) {

        String reservationKey = RedisKeyConstants.buildGrabReservationKey(reservationId);

        Long result = redisTemplate.execute(
                RedisScripts.CONFIRM_RESERVATION,
                List.of(reservationKey)
        );

        return convertConfirmResult(result);
    }

    private ReservationTransitionResult releaseReservation(
            String reservationId,
            Long screeningId,
            ReservationStatus targetStatus
    ) {

        String stockKey = RedisKeyConstants.buildScreeningStockKey(screeningId);
        String reservationKey = RedisKeyConstants.buildGrabReservationKey(reservationId);

        Long result = redisTemplate.execute(
                RedisScripts.RELEASE_RESERVATION,
                List.of(stockKey, reservationKey),
                targetStatus.name()
        );

        return convertReleaseResult(result);
    }

    private ReservationReserveOutcome convertReserveResult(List<?> result) {

        if (result == null || result.size() != 2) {
            throw new IllegalStateException("Redis 预留库存返回结果无效: " + result);
        }

        long code = toLong(result.get(0));
        long rawLeftStock = toLong(result.get(1));

        ReservationReserveResult reserveResult = switch ((int) code) {
            case 1 -> ReservationReserveResult.RESERVED;
            case 2 -> ReservationReserveResult.IDEMPOTENT;
            case -1 -> ReservationReserveResult.SOLD_OUT;
            case -2 -> ReservationReserveResult.INVALID_QUANTITY;
            case -3 -> ReservationReserveResult.NOT_READY;
            case -4 -> ReservationReserveResult.NOT_STARTED;
            case -5 -> ReservationReserveResult.SALE_CLOSED;
            default -> throw new IllegalStateException("未知的预留库存返回码: " + code);
        };

        Long leftStock = reserveResult == ReservationReserveResult.RESERVED ? rawLeftStock : null;

        return new ReservationReserveOutcome(reserveResult, leftStock);
    }

    private ReservationTransitionResult convertReleaseResult(Long result) {

        if (result == null) {
            throw new IllegalStateException("Redis 释放预留返回 null");
        }

        return switch (result.intValue()) {
            case 1 -> ReservationTransitionResult.APPLIED;
            case 2 -> ReservationTransitionResult.IDEMPOTENT;
            case -1 -> ReservationTransitionResult.NOT_FOUND;
            case -2 -> throw new IllegalStateException("非法的释放目标状态");
            case -3 -> ReservationTransitionResult.ILLEGAL_STATE;
            case -4 -> throw new IllegalStateException("无效的预留数量");
            case -5 -> ReservationTransitionResult.STOCK_MISSING;
            default -> throw new IllegalStateException("未知的释放预留返回结果：" + result);
        };
    }

    private long toLong(Object value) {

        if (value == null) {
            throw new IllegalStateException("Redis 脚本返回了 null 元素");
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        String raw;
        if (value instanceof byte[] bytes) {
            raw = new String(bytes, StandardCharsets.UTF_8);
        } else {
            raw = value.toString();
        }

        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Redis 脚本返回了非数字元素: " + raw + ", 类型=" + value.getClass().getName(), e);
        }
    }

    private ReservationTransitionResult convertConfirmResult(Long result) {

        if (result == null) {
            throw new IllegalStateException("Redis 确认预留返回 null");
        }

        return switch (result.intValue()) {
            case 1 -> ReservationTransitionResult.APPLIED;
            case 2 -> ReservationTransitionResult.IDEMPOTENT;
            case -1 -> ReservationTransitionResult.NOT_FOUND;
            case -2 -> ReservationTransitionResult.ILLEGAL_STATE;
            default -> throw new IllegalStateException("未知的确认预留返回结果：" + result);
        };
    }
}
