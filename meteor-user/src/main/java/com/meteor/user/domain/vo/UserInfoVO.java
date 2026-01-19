package com.meteor.user.domain.vo;

import com.meteor.user.enums.RoleEnum;
import lombok.Data;

/**
 * Info 接口 VO(带URL)
 *
 * @author Programmer
 * @date 2026-01-16 18:38
 */
@Data
public class UserInfoVO {

    private Long userId;

    private String username;

    private RoleEnum role;

    private String avatar;
}
