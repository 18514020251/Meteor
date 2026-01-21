package com.meteor.user.domain.assembler;

import com.meteor.minio.util.MinioUtil;
import com.meteor.user.domain.vo.UserInfoVO;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.service.cache.model.UserInfoCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *  用户信息转换
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
}
