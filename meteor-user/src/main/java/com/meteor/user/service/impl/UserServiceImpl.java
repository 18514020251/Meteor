package com.meteor.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.minio.enums.MinioPathEnum;
import com.meteor.minio.properties.MeteorMinioProperties;
import com.meteor.minio.util.MinioUtil;
import com.meteor.user.domain.dto.UserLoginReq;
import com.meteor.user.domain.dto.UserRegisterReq;
import com.meteor.user.domain.entiey.User;
import com.meteor.common.enums.DeleteStatus;
import com.meteor.user.domain.vo.UserInfoVO;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.enums.UserStatus;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.service.IUserService;
import com.meteor.common.utils.PasswordUtil;
import com.meteor.user.service.cache.IUserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;

    private final MinioUtil minioUtil;

    private final MeteorMinioProperties  minioProperties;

    private final IUserCacheService userCacheService;

    /*
     * 注册
     * */
    @Override
    public void register(UserRegisterReq req) {

        // todo： 验证码的获取/判断/

        registerNonEmpty(req);

        boolean exists = exists(req.getUsername());

        if (exists) {
            throw new BizException(CommonErrorCode.USER_EXIST);
        }

        User user = buildUser(req);

        userMapper.insert(user);
    }

    /*
     * 注册非空判断
     * */
    private void registerNonEmpty(UserRegisterReq req) {
        if (StringUtils.isBlank(req.getUsername())
                || StringUtils.isBlank(req.getPassword())) {
            throw new BizException(CommonErrorCode.PASSWORD_ERROR);
        }
    }

    /*
     * 查询用户是否存在
     * */
    private boolean exists(String username) {
        return this.exists(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
    }

    /*
     * 构建注册用户
     * */
    private User buildUser(UserRegisterReq req) {
        return User.builder()
                .username(req.getUsername())
                .password(PasswordUtil.encrypt(req.getPassword()))
                .status(UserStatus.NORMAL.getCode())
                .role(RoleEnum.USER.getCode())
                .isDeleted(DeleteStatus.NORMAL.getCode())
                .build();
    }


    /*
     * 登录
     * */
    @Override
    public String login(UserLoginReq req) {

        User user = getByUsername(req.getUsername());

        if (user == null) {
            throw new BizException(CommonErrorCode.PASSWORD_ERROR);
        }

        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(CommonErrorCode.PASSWORD_ERROR);
        }

        if (!user.isNormal()) {
            throw new BizException(CommonErrorCode.ACCOUNT_DISABLED);
        }

        // 登录
        StpUtil.login(user.getId());

        return StpUtil.getTokenValue();
    }


    /*
     * 根据用户名查询用户
     * */
    private User getByUsername(String username) {
        return getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getIsDeleted, DeleteStatus.NORMAL.getCode())
        );
    }


    /*
     *  获取当前用户信息
     * */
    @Override
    // todo： 后续增加功能，防击穿
    public UserInfoVO getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();

        UserInfoVO cached = userCacheService.getUserInfo(userId);
        if (cached != null) {
            return cached;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            userCacheService.cacheNull(userId);
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }

        UserInfoVO vo = buildUserInfoVO(user);
        userCacheService.cacheUserInfo(userId, vo);
        return vo;
    }


    /*
     *  构建用户信息 VO
     * */
    private UserInfoVO buildUserInfoVO(User user) {
        UserInfoVO resp = new UserInfoVO();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRole(RoleEnum.fromCode(user.getRole()));
        resp.setAvatar(user.getAvatar());
        return resp;
    }


    // todo: 后续桶改为private
    /*
    *  上传头像
    * */
    @Override
    public String uploadAvatar(MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();

        String objectName = minioUtil.upload(MinioPathEnum.USER_AVATAR.path(), file);

        String avatarUrl = buildAvatarUrl(objectName);

        updateUserAvatar(userId, avatarUrl);

        userCacheService.evictUserInfo(userId);

        return avatarUrl;
    }

    /*
    *  更新用户头像
    * */
    private void updateUserAvatar(Long userId, String avatarUrl) {
        User update = new User();
        update.setId(userId);
        update.setAvatar(avatarUrl);
        userMapper.updateById(update);
    }


    /*
    *  构建用户头像 URL
    * */
    private String buildAvatarUrl(String objectName) {
        return "http://127.0.0.1:9000/"
                + minioProperties.getBucket()
                + "/"
                + objectName;
    }

}
