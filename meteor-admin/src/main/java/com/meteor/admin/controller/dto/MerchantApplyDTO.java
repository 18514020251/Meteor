package com.meteor.admin.controller.dto;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *  商家申请查询参数
 *
 * @author Programmer
 * @date 2026-01-23 17:19
 */
@Data
public class MerchantApplyDTO {

    private Long applyId;
    private Long userId;
    private String shopName;
    private String applyReason;

    /** 状态码 0-PENDING, 1-APPROVED, 2-REJECTED */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    private Long reviewedBy;
    private LocalDateTime reviewedTime;
    private LocalDateTime createTime;

    public static MerchantApplyDTO fromEntity(MerchantApply entity) {
        MerchantApplyDTO dto = new MerchantApplyDTO();
        dto.setApplyId(entity.getApplyId());
        dto.setUserId(entity.getUserId());
        dto.setShopName(entity.getShopName());
        dto.setApplyReason(entity.getApplyReason());

        MerchantApplyStatusEnum status = entity.getStatus();
        dto.setStatus(status.getCode());
        dto.setStatusDesc(status.getDesc());

        dto.setReviewedBy(entity.getReviewedBy());
        dto.setReviewedTime(entity.getReviewedTime());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

}
