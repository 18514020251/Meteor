package com.meteor.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *  审核结果消息单条补发请求
 *
 * @author Programmer
 * @date 2026-01-29 13:42
 */
@Data
@Schema(description = "审核结果消息单条补发请求")
public class ReviewedResendDTO {

    @NotNull
    @Schema(description = "用户模块申请ID（applyId）", example = "12345")
    private Long applyId;
}
