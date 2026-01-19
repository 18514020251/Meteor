package com.meteor.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *  用户密码更新
 *
 * @author Programmer
 * @date 2026-01-19 15:51
 */
@Data
public class UserPasswordUpdateDTO {

    @Schema(description = "当前密码", example = "oldPassword123")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间")
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", example = "newPassword123")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @Schema(description = "确认密码", example = "newPassword123")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 之间")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
