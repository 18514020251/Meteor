package com.meteor.ticketing.service.reservation;

import com.meteor.ticketing.enums.ReservationReserveResult;

/**
 * Redis Reservation 预留结果。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-16
 */
public record ReservationReserveOutcome(
        ReservationReserveResult result,
        Long leftStock
) {
}
