package com.meteor.ticketing.enums;

/**
 * 库存预留状态
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-15
 */
public enum ReservationStatus {

    /* 库存已经完成预留，但业务尚未进入最终状态 */
    PRE_RESERVED,
    /* 预留已经被正式确认消费 */
    CONFIRMED,
    /* 因订单取消、超时等正常业务原因释放 */
    RELEASED,
    /* 因建单失败、事务失败等异常路径执行补偿 */
    COMPENSATED
}
