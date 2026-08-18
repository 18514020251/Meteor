package com.meteor.ticketing.service.transaction;

/**
 * Reservation + Outbox 本地事务在事务方法体内明确失败。
 * 该异常表示：
 * 1. 事务目标方法尚未正常返回；
 * 2. 异常会传播出 @Transactional 方法；
 * 3. Spring 将对本地事务执行 rollback。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-18
 */
public class ReservationOutboxRollbackException extends RuntimeException {

    public ReservationOutboxRollbackException(String message) {
        super(message);
    }

    public ReservationOutboxRollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
