package com.meteor.user.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *  用户登录响应
 *
 * @author Programmer
 * @date 2026-02-03 20:39
 */
@Schema(description = "用户登录响应")
public record UserLoginVO(

    @Schema(description = "token")
    String token,

    @Schema(description = "用户ID")
    Long userId,

    @Schema(description = "角色")
    String role,

    @Schema(description = "是否需要首次引导(选择喜好分类)")
    boolean needOnboarding
){}
