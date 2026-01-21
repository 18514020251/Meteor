package com.meteor.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.meteor.common.enums.DeleteStatus;
import com.meteor.user.enums.merchant.MerchantApplyStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户申请成为商家表
 * </p>
 *
 * @author Programmer
 * @since 2026-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_apply")
@Schema(description = "用户申请成为商家表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantApply implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键 ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "店铺名称", example = "Meteor 店铺")
    private String shopName;

    @Schema(description = "申请备注/说明", example = "本人有实体店铺，申请成为商家")
    private String applyReason;

    @Schema(description = "申请状态：0-待审核，1-已通过，2-已拒绝", example = "0")
    private Integer status;

    @Schema(description = "管理员审核备注", example = "资料不完整")
    private String auditRemark;

    @Schema(description = "审核人 ID", example = "20001")
    private Long auditBy;

    @Schema(description = "审核时间", example = "2026-01-20 18:30:00")
    private LocalDateTime auditTime;

    @Schema(description = "创建时间", example = "2026-01-20 17:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-20 18:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;


    /**
     * 是否待审核
     */
    public boolean isPending() {
        return MerchantApplyStatusEnum.PENDING.getCode().equals(this.status);
    }

    /**
     * 是否已通过
     */
    public boolean isApproved() {
        return MerchantApplyStatusEnum.APPROVED.getCode().equals(this.status);
    }

    /**
     * 是否已拒绝
     */
    public boolean isRejected() {
        return MerchantApplyStatusEnum.REJECTED.getCode().equals(this.status);
    }

    /**
     * 是否已删除
     */
    public boolean deleted() {
        return DeleteStatus.DELETED.getCode().equals(this.isDeleted);
    }

    /*
    *  创建待审核申请
    * */
    public static MerchantApply pending(Long userId, String shopName, String applyReason) {
        return MerchantApply.builder()
                .userId(userId)
                .shopName(shopName)
                .applyReason(applyReason)
                .status(MerchantApplyStatusEnum.PENDING.getCode())
                .build();
    }

}
