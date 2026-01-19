package com.meteor.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.constants.AvatarConstants;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.minio.enums.MinioPathEnum;
import com.meteor.minio.properties.MeteorMinioProperties;
import com.meteor.minio.util.MinioUtil;
import com.meteor.user.domain.dto.UserLoginReq;
import com.meteor.user.domain.dto.UserPasswordUpdateDTO;
import com.meteor.user.domain.dto.UserProfileUpdateDTO;
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

import static com.meteor.common.constants.AvatarConstants.ALLOWED_TYPES;
import static com.meteor.common.constants.AvatarConstants.MAX_SIZE;


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
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
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
                .avatar(AvatarConstants.DEFAULT_AVATAR)
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

        User user = getNormalUserByUsername(req.getUsername());

        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
        }

        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }

    /*
    *  根据用户名获取用户信息
    * */
    private User getNormalUserByUsername(String username) {
        User user = getByUsername(username);

        if (user == null) {
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
        }

        if (user.isDeleted()) {
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
        }

        if (!user.isNormal()) {
            throw new BizException(CommonErrorCode.ACCOUNT_DISABLED);
        }

        return user;
    }

    /*
     * 根据用户名查询用户
     * */
    private User getByUsername(String username) {
        return getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
    }


    /*
     *  获取当前用户信息
     * */
    @Override
    // todo： 后续增加功能，防击穿
    public UserInfoVO getCurrentUserInfo(Long userId) {
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
        resp.setAvatar(minioUtil.buildObjectUrl(user.getAvatar()));
        return resp;
    }


    // todo: 后续桶改为private
    /*
    *  上传头像
    * */
    @Override
    public String uploadAvatar(MultipartFile file , Long userId) {

        if (file.getSize() > MAX_SIZE) {
            throw new BizException(CommonErrorCode.AVATAR_SIZE_ERROR);
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BizException(CommonErrorCode.AVATAR_TYPE_ERROR);
        }

        String objectName = minioUtil.upload(MinioPathEnum.USER_AVATAR.path(), file);

        updateUserAvatar(userId, objectName);

        userCacheService.evictUserInfo(userId);

        return minioUtil.buildObjectUrl(objectName);
    }

    /*
    *  更新用户头像
    * */
    private void updateUserAvatar(Long userId, String newAvatar) {
        User oldUser = userMapper.selectById(userId);

        User update = new User();
        update.setId(userId);
        update.setAvatar(newAvatar);
        userMapper.updateById(update);

        deleteUserAvatar(oldUser);
    }

    /*
    *  删除用户及关联信息
    * */
    @Override
    public void deleteUserAndRelatedInfo(Long userId) {
        User user = userMapper.selectById(userId);

        StpUtil.logout();
        userCacheService.evictUserInfo(userId);

        userMapper.deleteById(userId);
        deleteUserAvatar(user);
    }

    /*
    *  删除用户头像
    * */
    private void deleteUserAvatar(User user){
        if (user.getAvatar() != null && !user.getAvatar().equals(AvatarConstants.DEFAULT_AVATAR)) {
            minioUtil.delete(user.getAvatar());
            log.info("删除用户头像：用户ID{}", user.getId());
        }
    }

    /*
    *  更新用户信息
    * */
    @Override
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {

        User user = userMapper.selectById(userId);
        if (user == null || user.isDeleted()) {
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }

        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUsername())) {
            checkUsernameUnique(dto.getUserName(), userId);
            user.setUsername(dto.getUserName());
        }

        if (dto.getPhone() != null && !dto.getPhone().equals(user.getPhone())) {
            checkPhoneUnique(dto.getPhone(), userId);
            user.setPhone(dto.getPhone());
        }

        userCacheService.evictUserInfo(userId);

        userMapper.updateById(user);
    }

    /*
    *  检查用户名唯一
    * */
    private void checkUsernameUnique(String username, Long userId) {
        boolean exists = lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, DeleteStatus.NORMAL.getCode())
                .ne(User::getId, userId)
                .exists();

        if (exists) {
            throw new BizException(CommonErrorCode.USER_EXIST);
        }
    }

    /*
    *  检查手机号唯一
    * */
    private void checkPhoneUnique(String phone, Long userId) {
        boolean exists = lambdaQuery()
                .eq(User::getPhone, phone)
                .eq(User::getIsDeleted, DeleteStatus.NORMAL.getCode())
                .ne(User::getId, userId)
                .exists();

        if (exists) {
            throw new BizException(CommonErrorCode.PHONE_EXIST);
        }
    }

    /*
    *  更新密码
    * */
    @Override
    public void updatePassword(Long userId, UserPasswordUpdateDTO dto) {

        User user = userMapper.selectById(userId);
        if (user == null || user.isDeleted()) {
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }

        if (!PasswordUtil.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(CommonErrorCode.PASSWORD_ERROR);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(CommonErrorCode.PASSWORD_CONFIRM_ERROR);
        }

        user.setPassword(PasswordUtil.encrypt(dto.getNewPassword()));
        userMapper.updateById(user);
    }

}
