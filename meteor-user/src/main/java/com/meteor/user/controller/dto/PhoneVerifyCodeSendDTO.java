package com.meteor.user.controller.dto;

import com.meteor.common.enums.user.VerifyCodeSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


/**
 *  手机验证码发送 DTO
 *
 * @author Programmer
 * @date 2026-01-20 22:45
 */
@Data
public class PhoneVerifyCodeSendDTO {

    @Schema(description = "手机号", example = "13800000000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确"
    )
    private String phone;

    @Schema(description = "手机验证码", example = "123456")
    @NotNull(message = "手机验证码不能为空")
    private VerifyCodeSceneEnum scene;
}
