package com.meteor.merchant.domain.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.merchant.enums.MerchantStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商家表
 * </p>
 *
 * @author Programmer
 * @since 2026-01-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant")
@Schema(description="商家表")
public class Merchant implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商家ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联用户ID（唯一）")
    private Long userId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺公告/简介（短）")
    private String notice;

    @Schema(description = "商家状态：0-正常 1-冻结 2-关闭")
    private MerchantStatusEnum status;

    @Schema(description = "审核通过/开通时间")
    private LocalDateTime verifiedTime;

    @Schema(description = "来源申请ID（用于幂等/追溯）")
    private Long applyId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "软删：0-未删 1-已删")
    @TableLogic
    private DeleteStatus isDeleted;

    public boolean canEditProfile() {
        return this.status == MerchantStatusEnum.NORMAL;
    }
}
