package com.meteor.analytics.controller.vo;

import com.meteor.analytics.service.IOpMqFailMsgService;

/**
 *  补发单条结果
 *
 * @author Programmer
 * @date 2026-02-13 22:37
 * @version 1.0
 */
public record ResendOneVO(
        String requestId,
        Long id,
        String msgId,
        boolean locked,
        boolean success,
        String error
) {
    public static ResendOneVO from(IOpMqFailMsgService.ResendOneResult r) {
        return new ResendOneVO(r.requestId(), r.id(), r.msgId(), r.locked(), r.success(), r.error());
    }
}
