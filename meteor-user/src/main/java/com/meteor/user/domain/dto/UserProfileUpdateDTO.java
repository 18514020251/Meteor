package com.meteor.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *  用户信息更新
 *
 * @author Programmer
 * @date 2026-01-19 14:25
 */
@Data
public class UserProfileUpdateDTO {

    @Schema(description = "用户名" , example = "username123")
    @Size(min = 1, max = 20, message = "用户名长度必须在 1-20 之间")
    private String username;

    @Schema(description = "手机号" , example = "13800000000")
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确"
    )
    private String phone;
}


