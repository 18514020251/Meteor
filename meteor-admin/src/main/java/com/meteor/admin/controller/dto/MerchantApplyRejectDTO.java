package com.meteor.admin.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *  商家申请拒绝参数
 *
 * @author Programmer
 * @date 2026-01-28 11:47
 */
@Data
public class MerchantApplyRejectDTO {
    @NotBlank(message = "拒绝原因不能为空")
    private String rejectReason;
}
