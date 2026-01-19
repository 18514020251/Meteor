package com.meteor.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String oldPassword;

    @Schema(description = "新密码", example = "newPassword123")
    private String newPassword;

    @Schema(description = "确认密码", example = "newPassword123")
    private String confirmPassword;
}
