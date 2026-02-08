package com.meteor.user.service.assembler;

import com.meteor.api.contract.user.dto.UserProfileDTO;
import com.meteor.minio.util.MinioUtil;
import com.meteor.user.controller.vo.UserInfoVO;
import com.meteor.user.controller.vo.UserLoginVO;
import com.meteor.user.domain.entity.User;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.enums.UserPreferenceInitEnum;
import com.meteor.user.service.cache.model.UserInfoCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *  用户信息转换 工具类
 *
 * @author Programmer
 */
@Component
@RequiredArgsConstructor
public class UserInfoAssembler {

    private final MinioUtil minioUtil;

    public UserInfoVO toVO(UserInfoCache cache) {
        return new UserInfoVO(
                cache.getUserId(),
                cache.getUsername(),
                RoleEnum.fromCode(cache.getRole()),
                minioUtil.buildPresignedUrl(cache.getAvatarObject()),
                cache.getPhone()
        );
    }

    public UserProfileDTO toProfile(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setAvatar(minioUtil.buildPresignedUrl(user.getAvatar()));
        return dto;
    }

    public UserLoginVO toLoginVo(String tokenValue, User user) {
        RoleEnum roleEnum = RoleEnum.fromCode(user.getRole());
        return new UserLoginVO(
                tokenValue,
                user.getId(),
                roleEnum == null ? RoleEnum.USER.getDesc() : roleEnum.getDesc(),
                user.getPreferenceInited() == null
                        || user.getPreferenceInited() == UserPreferenceInitEnum.NOT_INIT  // needOnboarding 对应第四个参数
        );
    }
}
