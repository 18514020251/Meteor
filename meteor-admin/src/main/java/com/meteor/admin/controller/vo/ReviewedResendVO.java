package com.meteor.admin.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *  商家申请审核重发VO
 *
 * @author Programmer
 * @date 2026-01-29 13:43
 */
@Data
@AllArgsConstructor
@Schema(description = "审核结果消息单条补发响应")
public class ReviewedResendVO {

    @Schema(description = "是否补发成功")
    private Boolean success;

    @Schema(description = "是否已发送（幂等命中）")
    private Boolean alreadySent;

    @Schema(description = "提示或失败原因")
    private String message;
}