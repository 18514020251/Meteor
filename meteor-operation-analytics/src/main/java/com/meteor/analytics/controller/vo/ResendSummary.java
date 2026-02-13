package com.meteor.analytics.controller.vo;

import com.meteor.analytics.service.IOpMqFailMsgService;

/**
 * 补发结果汇总
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 22:23
 */
public record ResendSummary(
        String requestId,
        long totalCandidates,
        long locked,
        long success,
        long failed,
        java.util.List<IOpMqFailMsgService.ResendOneResult> skipped
) {}
