package com.meteor.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户申请成为商家表
 * @author Programmer
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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String shopName;

    private String applyReason;

    private MerchantApplyStatusEnum status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


    public boolean isPending() {
        return this.status == MerchantApplyStatusEnum.PENDING;
    }

    public boolean isApproved() {
        return this.status == MerchantApplyStatusEnum.APPROVED;
    }

    public boolean isRejected() {
        return this.status == MerchantApplyStatusEnum.REJECTED;
    }

    /**
     * 创建待审核申请
     */
    public static MerchantApply pending(Long userId, String shopName, String applyReason) {
        return MerchantApply.builder()
                .userId(userId)
                .shopName(shopName)
                .applyReason(applyReason)
                .status(MerchantApplyStatusEnum.PENDING)
                .build();
    }
}
