package com.meteor.admin.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 *  商家申请未发送的视图
 *
 * @author Programmer
 * @date 2026-01-29 12:08
 */
@Data
public class MerchantApplyUnsentVO {
    private Long applyId;
    private Long userId;
    private String shopName;
    private Integer status;

    private Long reviewedBy;
    private LocalDateTime reviewedTime;

    private Integer reviewedMsgSent;
    private LocalDateTime reviewedMsgSentTime;

    private String rejectReason;
}
