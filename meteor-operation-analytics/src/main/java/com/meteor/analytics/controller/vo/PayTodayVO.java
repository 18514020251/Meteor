package com.meteor.analytics.controller.vo;

/**
 *  今日支付汇总
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 20:10
 */
public record PayTodayVO(
        int payAttemptCnt,
        int paySuccessCnt,
        long gmvCent,
        int dealOrderCnt
) {
    public static final PayTodayVO ZERO = new PayTodayVO(0,0,0L,0);
}


