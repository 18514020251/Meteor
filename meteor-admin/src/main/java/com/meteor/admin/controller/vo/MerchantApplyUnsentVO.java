package com.meteor.admin.controller.vo;

import java.time.LocalDateTime;

/**
 *  商家申请未发送的视图
 *
 * @author Programmer
 * @date 2026-01-29 12:08
 */
public record MerchantApplyUnsentVO(
    Long applyId,
    Long userId,
    String shopName,
    Integer status,

    Long reviewedBy,
    LocalDateTime reviewedTime,

    Integer reviewedMsgSent,
    LocalDateTime reviewedMsgSentTime,

    String rejectReason
){}
