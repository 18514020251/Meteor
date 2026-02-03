package com.meteor.user.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *  用户登录响应
 *
 * @author Programmer
 * @date 2026-02-03 20:39
 */
@Data
@Schema(description = "用户登录响应")
public class UserLoginVO {

    @Schema(description = "token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "是否需要首次引导(选择喜好分类)")
    private boolean needOnboarding;
}
