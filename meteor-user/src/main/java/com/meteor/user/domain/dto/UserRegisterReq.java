package com.meteor.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *  用户注册
 *
 * @author Programmer
 * @date 2026-01-16 16:52
 */
@Data
public class UserRegisterReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

