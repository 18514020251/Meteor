package com.meteor.api.contract.user.dto;

import lombok.Data;

/**
 *  用户信息 DTO
 *  商家模块远程调用用户模块返回数据
 *
 * @author Programmer
 * @date 2026-02-01 11:29
 */
@Data
public class UserProfileDTO {
    // 用户名
    private String username;
    // 手机号
    private String phone;
    // 头像
    private String avatar;
}
