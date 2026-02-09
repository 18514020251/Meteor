package com.meteor.ticketing.controller.vo;

import com.meteor.ticketing.enums.RedisStockResultEnum;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-09 21:21
 */
public record RedisDecrStockResult(RedisStockResultEnum code, Long left) {
    public boolean ok() { return code == RedisStockResultEnum.SUCCESS; }
}
