package com.meteor.admin.mq.merchant;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  商家申请审核消息
 *
 * @author Programmer
 * @date 2026-01-27 10:29
 */
@Data
public class MerchantApplyReviewedMessage implements Serializable {

    private Long applyId;

    /** APPROVED / REJECTED */
    private String status;

    private String rejectReason;

    private Long reviewedBy;

    private LocalDateTime reviewedTime;
}
