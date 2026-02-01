package com.meteor.user.service.assembler;

import com.meteor.common.dto.UserProfileDTO;
import com.meteor.user.domain.entity.User;

/**
 *  用户信息转换 static
 *
 * @author Programmer
 * @date 2026-02-01 11:50
 */
public final class UserProfileAssembler {

    private UserProfileAssembler() {}

    public static UserProfileDTO toProfile(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        return dto;
    }
}
