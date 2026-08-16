package com.meteor.ticketing.enums;

/**
 * Reservation 状态转换结果。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-16
 */
public enum ReservationTransitionResult {

    /**
     * 本次真正完成了状态转换。
     */
    APPLIED,

    /**
     * 已经处于目标状态，
     * 本次属于幂等重放。
     */
    IDEMPOTENT,

    /**
     * Reservation 不存在。
     */
    NOT_FOUND,

    /**
     * 当前状态不允许执行该转换。
     */
    ILLEGAL_STATE,

    /**
     * Redis stock key 缺失。
     *
     * 只有需要恢复库存的 release / compensate
     * 才可能出现。
     */
    STOCK_MISSING,


    /**
     * Redis 预检失败。
     *
     * 只有需要预检的 compensate
     * 才可能出现。
     */
    SCREENING_MISMATCH
}
