package com.meteor.merchant.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *  修改商家公告 DTO
 *
 * @author Programmer
 * @date 2026-02-01 11:17
 */
@Data
public class UpdateShopNoticeDTO {

    @NotBlank(message = "店铺公告不能为空")
    @Max(value = 200, message = "店铺公告不能超过200个字符")
    private String notice;

}
