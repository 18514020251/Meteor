package com.meteor.common.dto;

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
    private String username;
    private String phone;
    private String avatar;
}
