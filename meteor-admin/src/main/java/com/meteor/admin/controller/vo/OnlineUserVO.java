package com.meteor.admin.controller.vo;

/**
 *  在线用户信息
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 21:58
 */
public record OnlineUserVO(
        String userId,
        String ip,
        String role,
        Long loginTime
) {}

