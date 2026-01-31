package com.meteor.user.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *  用户申请成为商家 DTO
 *
 * @author Programmer
 * @date 2026-01-20 23:30
 */
@Data
@Schema(description = "用户申请成为商家 DTO")
public class MerchantApplyDTO {

    @Schema(description = "店铺名称" , example = "MeteorShop")
    @NotBlank
    @Size(max = 100)
    private String shopName;

    @Schema(description = "申请说明", example = "本人有线下门店，申请成为商家")
    @Size(max = 255)
    private String applyReason;
}
