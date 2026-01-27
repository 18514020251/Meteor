package com.meteor.common.mq.merchant;

import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  商家申请审核完成消息
 *
 * @author Programmer
 * @date 2026-01-27 12:16
 */
@Data
public class MerchantApplyReviewedMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户侧申请ID（user.merchant_apply.id） */
    private Long applyId;

    /** 审核结果：0-PENDING 1-APPROVED 2-REJECTED */
    private MerchantApplyStatusEnum status;

    /** 拒绝原因（通过时可为 null） */
    private String rejectReason;

    /** 审核人ID */
    private Long reviewedBy;

    /** 审核时间 */
    private LocalDateTime reviewedTime;
}
