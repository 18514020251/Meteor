package com.meteor.ticketing.enums;

/**
 *  保存预约结果枚举
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-17
 */
public enum ReservationPersistResult {

    /**
     * 本次真正插入了新的 Reservation。
     */
    CREATED,

    /**
     * 相同 Reservation 已经存在，本次是幂等重放。
     */
    IDEMPOTENT,

    /**
     * 唯一身份发生冲突。
     */
    CONFLICT
}
