package com.meteor.ticketing.enums;

import lombok.Getter;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 21:17
 */
@Getter
public enum RedisStockResultEnum {

    SUCCESS("成功"),
    SOLD_OUT("库存不足"),
    NOT_READY("库存未预热"),
    ERROR("Redis执行异常");

    private final String desc;

    RedisStockResultEnum(String desc) {
        this.desc = desc;
    }

    /**
     * 扣库存脚本解析（DECR）
     */
    public static RedisStockResultEnum fromDecrResult(Long result) {
        if (result == null) return ERROR;
        if (result >= 0) return SUCCESS;
        if (result == -1) return SOLD_OUT;
        if (result == -3) return NOT_READY;
        return ERROR;
    }

    /**
     * 回滚库存脚本解析（INCR）
     * INCR 不会出现 SOLD_OUT
     */
    public static RedisStockResultEnum fromIncrResult(Long result) {
        if (result == null) return ERROR;
        if (result >= 0) return SUCCESS;
        if (result == -3) return NOT_READY;
        return ERROR;
    }
}

