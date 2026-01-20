package com.meteor.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Programmer
 * @date 2026-01-20 23:30
 */
@Data
@Schema(description = "用户申请成为商家 DTO")
public class MerchantApplyDTO {

    @Schema(description = "店铺名称" , example = "MeteorShop")
    private String shopName;

    @Schema(description = "申请说明", example = "本人有线下门店，申请成为商家")
    private String applyReason;
}
