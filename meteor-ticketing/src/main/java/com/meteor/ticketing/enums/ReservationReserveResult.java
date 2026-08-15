package com.meteor.ticketing.enums;

/**
 * Redis Reservation reserve 结果。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-16
 */
public enum ReservationReserveResult {

    /**
     * 本次真正创建了 PRE_RESERVED，
     * 并完成库存扣减。
     */
    RESERVED,

    /**
     * Reservation 已经存在，
     * 本次属于幂等重放。
     */
    IDEMPOTENT,

    /**
     * 库存不足。
     */
    SOLD_OUT,

    /**
     * quantity 非法。
     */
    INVALID_QUANTITY,

    /**
     * Redis 销售窗口 / 库存元数据缺失。
     */
    NOT_READY,

    /**
     * 尚未开售。
     */
    NOT_STARTED,

    /**
     * 已经停售。
     */
    SALE_CLOSED
}
