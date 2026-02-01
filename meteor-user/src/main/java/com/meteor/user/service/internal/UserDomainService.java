package com.meteor.user.service.internal;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.user.domain.entity.User;
import com.meteor.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *  用户领域服务
 *
 * @author Programmer
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserMapper userMapper;

    /**
     * 根据 userId 获取有效用户（存在 + 未删除）
     */
    public User getValidUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.deleted()) {
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 根据手机号获取有效用户（存在 + 未删除）
     */
    public User getValidUserByPhone(String phone) {
        User user = userMapper.selectByPhone(phone);
        if (user == null || user.deleted()) {
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 根据用户名获取用户信息
     * */
    public User getNormalUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);

        if (user == null) {
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
        }

        if (user.deleted()) {
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
        }

        if (!user.isNormal()) {
            throw new BizException(CommonErrorCode.ACCOUNT_DISABLED);
        }

        return user;
    }

}
