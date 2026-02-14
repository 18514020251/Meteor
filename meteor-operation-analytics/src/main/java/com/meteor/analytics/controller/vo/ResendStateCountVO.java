package com.meteor.analytics.controller.vo;

/**
 *  重发消息状态统计
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-14 9:59
 */
public record ResendStateCountVO(
        String resendStateDesc,
        Integer resendState,
        Long count
) {
}
