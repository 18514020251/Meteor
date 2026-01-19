package com.meteor.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *  根据手机号修改密码
 *
 * @author Programmer
 * @date 2026-01-19 17:04
 */
@Data
public class UserPasswordResetByPhoneDTO {

    @Schema(description = "手机号", example = "13800000000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确"
    )
    private String phone;

    @Schema(description = "新密码", example = "123456")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 位之间")
    private String newPassword;

    @Schema(description = "确认密码", example = "123456")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}

