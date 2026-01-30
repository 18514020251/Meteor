package com.meteor.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import com.meteor.common.enums.message.MessageSendStatusEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家申请表（管理端，审核视图）
 *
 * @author Programmer
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_apply")
@Schema(description = "商家申请表（管理端，审核视图）")
public class MerchantApply implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long applyId;

    private Long userId;

    private String shopName;

    private String applyReason;

    /**
     * 状态：PENDING / APPROVED / REJECTED
     */
    private MerchantApplyStatusEnum status;

    private String rejectReason;

    private Long reviewedBy;

    private LocalDateTime reviewedTime;

    private LocalDateTime createTime;

    private Integer reviewedMsgSent;

    private LocalDateTime reviewedMsgSentTime;


    public void approve(Long reviewerId, LocalDateTime reviewedTime) {
        if (this.status != MerchantApplyStatusEnum.PENDING) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_ALREADY_REVIEWED);
        }
        this.status = MerchantApplyStatusEnum.APPROVED;
        this.rejectReason = null;
        this.reviewedBy = reviewerId;
        this.reviewedTime = reviewedTime;
        this.reviewedMsgSent = MessageSendStatusEnum.UNSENT.getCode();
        this.reviewedMsgSentTime = null;
    }

    public void reject(Long reviewerId, LocalDateTime reviewedTime, String rejectReason) {
        if (this.status != MerchantApplyStatusEnum.PENDING) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_ALREADY_REVIEWED);
        }
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_REJECT_REASON_REQUIRED);
        }
        this.status = MerchantApplyStatusEnum.REJECTED;
        this.rejectReason = rejectReason.trim();
        this.reviewedBy = reviewerId;
        this.reviewedTime = reviewedTime;
        this.reviewedMsgSent = MessageSendStatusEnum.UNSENT.getCode();
        this.reviewedMsgSentTime = null;
    }
}
