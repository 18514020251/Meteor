package com.meteor.user.controller.vo;

import com.meteor.user.enums.RoleEnum;

/**
 * Info 接口 VO(带URL)
 *
 * @author Programmer
 * @date 2026-01-16 18:38
 */
public record UserInfoVO(
        Long userId,
        String username,
        RoleEnum role,
        String avatar,
        String phone
) {
}
