package com.meteor.ticketing.service.reservation;

import com.meteor.ticketing.enums.ReservationStatus;

import static com.meteor.ticketing.enums.ReservationStatus.PRE_RESERVED;

/**
 * Reservation 状态转换规则。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-15
 */
public class ReservationStateMachine {

    /**
     * 执行状态转换。
     *
     * @param current 当前状态
     * @param target  目标状态
     * @return 转换后的状态
     * @throws IllegalArgumentException 如果 current 或 target 为 null
     * @throws IllegalStateException    如果状态转换非法
     */
    public ReservationStatus transition(
            ReservationStatus current,
            ReservationStatus target
    ) {
        if (current == null) {
            throw new IllegalArgumentException("当前预约状态不能为空");
        }

        if (target == null) {
            throw new IllegalArgumentException("目标预约状态不能为空");
        }

        // 同状态重放统一幂等
        if (current == target) {
            return current;
        }

        // 只有 PRE_RESERVED 可以进入其他终态
        if (current == PRE_RESERVED) {
            return target;
        }

        throw new IllegalStateException("非法的预约状态转换: " + current + " -> " + target);
    }
}