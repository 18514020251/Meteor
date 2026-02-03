package com.meteor.user.service.assembler;

import com.meteor.common.dto.UserProfileDTO;
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
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(cache.getUserId());
        vo.setUsername(cache.getUsername());
        vo.setRole(RoleEnum.fromCode(cache.getRole()));
        vo.setAvatar(minioUtil.buildPresignedUrl(cache.getAvatarObject()));
        vo.setPhone(cache.getPhone());
        return vo;
    }

    public UserProfileDTO toProfile(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setAvatar(minioUtil.buildPresignedUrl(user.getAvatar()));
        return dto;
    }

    public UserLoginVO toLoginVo(String tokenValue, User user) {
        UserLoginVO vo = new UserLoginVO();
        vo.setToken(tokenValue);
        RoleEnum roleEnum = RoleEnum.fromCode(user.getRole());
        vo.setRole(roleEnum == null ? RoleEnum.USER.getDesc() : roleEnum.getDesc());
        vo.setNeedOnboarding(
                user.getPreferenceInited() == null
                        || user.getPreferenceInited() == UserPreferenceInitEnum.NOT_INIT
        );
        vo.setUserId(user.getId());
        return vo;
    }
}
