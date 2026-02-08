package com.meteor.admin.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *  商家申请审核重发VO
 *
 * @author Programmer
 * @date 2026-01-29 13:43
 */
@Schema(description = "审核结果消息单条补发响应")
public record ReviewedResendVO(

    @Schema(description = "是否补发成功")
    Boolean success,

    @Schema(description = "是否已发送（幂等命中）")
    Boolean alreadySent,

    @Schema(description = "提示或失败原因")
    String message
){}