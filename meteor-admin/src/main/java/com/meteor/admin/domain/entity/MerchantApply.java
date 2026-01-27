package com.meteor.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;

/**
 * <p>
 * 商家申请表（管理端，审核视图）
 * </p>
 *
 * @author Programmer
 * @since 2026-01-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_apply")
@Schema(description="商家申请表（管理端，审核视图）")
public class MerchantApply implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "管理端记录 ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户模块的申请 ID")
    private Long applyId;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "申请理由")
    private String applyReason;

    @Schema(description = "状态：0-PENDING 1-APPROVED 2-REJECTED")
    private Integer status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核人 ID")
    private Long reviewedBy;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedTime;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    public void setStatus(MerchantApplyStatusEnum status) {
        this.status = status.getCode();
    }

    public void approve(Long reviewerId, LocalDateTime reviewedTime) {
        if (!MerchantApplyStatusEnum.PENDING.getCode().equals(this.status)) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_ALREADY_REVIEWED);
        }
        this.setStatus(MerchantApplyStatusEnum.APPROVED);
        this.rejectReason = null;
        this.reviewedBy = reviewerId;
        this.reviewedTime = reviewedTime;
    }

    public void reject(Long reviewerId, LocalDateTime reviewedTime, String rejectReason) {
        if (!MerchantApplyStatusEnum.PENDING.getCode().equals(this.status)) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_ALREADY_REVIEWED);
        }
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new BizException(CommonErrorCode.MERCHANT_APPLY_REJECT_REASON_REQUIRED);
        }
        this.setStatus(MerchantApplyStatusEnum.REJECTED);
        this.rejectReason = rejectReason.trim();
        this.reviewedBy = reviewerId;
        this.reviewedTime = reviewedTime;
    }
}
