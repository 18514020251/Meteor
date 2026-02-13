package com.meteor.analytics.controller.vo;

import com.meteor.analytics.service.IOpMqFailMsgService;

/**
 *  补发结果
 *
 * @author Programmer
 * @date 2026-02-13 22:37
 * @version 1.0
 */
public record ResendAllVO(
        String requestId,
        long totalCandidates, // 扫描到的候选数（例如 state != SUCCESS）
        long locked,          // 抢锁成功数（真正执行补发的）
        long success,
        long failed,
        java.util.List<IOpMqFailMsgService.ResendOneResult> skipped          // 未抢到锁/不符合状态等（避免重复补发）
) {
    public static ResendAllVO from(IOpMqFailMsgService.ResendSummary s) {
        return new ResendAllVO(
                s.requestId(),
                s.totalCandidates(),
                s.locked(),
                s.success(),
                s.failed(),
                s.skipped()
        );
    }
}

