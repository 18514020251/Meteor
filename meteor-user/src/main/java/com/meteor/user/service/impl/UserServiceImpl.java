package com.meteor.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.constants.AvatarConstants;
import com.meteor.common.enums.DeleteStatus;
import com.meteor.common.enums.VerifyCodeSceneEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.utils.PasswordUtil;
import com.meteor.common.utils.PhoneUtil;
import com.meteor.common.utils.image.ImageCropUtil;
import com.meteor.minio.enums.MinioPathEnum;
import com.meteor.minio.util.MinioUtil;
import com.meteor.user.domain.assembler.UserInfoAssembler;
import com.meteor.user.domain.dto.*;
import com.meteor.user.domain.entity.User;
import com.meteor.user.domain.vo.UserInfoVO;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.service.IUserService;
import com.meteor.user.service.cache.IPhoneCodeCacheService;
import com.meteor.user.service.cache.IPhoneCodeLimitCacheService;
import com.meteor.user.service.cache.IUserCacheService;
import com.meteor.user.service.cache.model.UserInfoCache;
import com.meteor.user.service.domain.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.meteor.common.constants.AvatarConstants.ALLOWED_TYPES;
import static com.meteor.common.constants.AvatarConstants.MAX_SIZE;
import static com.meteor.common.exception.CommonErrorCode.*;


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

    private final UserDomainService userDomainService;

    private final IUserCacheService userCacheService;

    private final IPhoneCodeCacheService phoneCodeCacheService;

    private final IPhoneCodeLimitCacheService phoneCodeLimitCacheService;

    private final UserInfoAssembler   userInfoAssembler;


    /*
    *  注册
    * */
    @Override
    public void register(UserRegisterReq req) {

        if (req == null
                || StringUtils.isBlank(req.getUsername())
                || StringUtils.isBlank(req.getPassword())) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        boolean exists = lambdaQuery()
                .eq(User::getUsername, req.getUsername())
                .eq(User::getIsDeleted, DeleteStatus.NORMAL.getCode())
                .exists();

        if (exists) {
            throw new BizException(CommonErrorCode.USER_EXIST);
        }

        User user = User.createRegisterUser(
                req.getUsername(),
                req.getPassword()
        );

        userMapper.insert(user);
    }


    /*
    *  登录
    * */
    @Override
    public String login(UserLoginReq req) {

        User user = userDomainService.getNormalUserByUsername(req.getUsername());

        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
        }

        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }


    /*
    *  获取用户当前信息
    * */
    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {

        UserInfoCache cache = userCacheService.getUserInfo(userId);
        if (cache != null) {
            return userInfoAssembler.toVO(cache);
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.deleted()) {
            userCacheService.cacheNull(userId);
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }

        UserInfoCache newCache = UserInfoCache.fromUser(user);
        userCacheService.cacheUserInfo(userId, newCache);

        return userInfoAssembler.toVO(newCache);

    }
    /*
    *  上传头像
    * */
    // todo: 除去核心功能外，其余代码过多，而且导致复用性、可读性查，待优化
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {

        validateAvatar(file);

        InputStream processedStream;
        try {
            processedStream = ImageCropUtil.cropToSquare(
                    file.getInputStream(),
                    getImageFormat(file)
            );
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.IMAGE_PROCESS_ERROR);
        }


        String objectName = minioUtil.upload(
                MinioPathEnum.USER_AVATAR.path(),
                processedStream,
                file.getContentType()
        );

        updateUserAvatar(userId, objectName);
        userCacheService.evictUserInfo(userId);

        return minioUtil.buildPresignedUrl(objectName);
    }

    /*
    *  获取图片格式
    * */
    private String getImageFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BizException(CommonErrorCode.AVATAR_TYPE_ERROR);
        }

        if ("image/png".equals(contentType)) {
            return "png";
        }
        if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
            return "jpg";
        }

        throw new BizException(CommonErrorCode.AVATAR_TYPE_ERROR);
    }



    /*
    *  验证头像
    * */
    private void validateAvatar(MultipartFile file) {

        if (file.getSize() > MAX_SIZE) {
            throw new BizException(CommonErrorCode.AVATAR_SIZE_ERROR);
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BizException(CommonErrorCode.AVATAR_TYPE_ERROR);
        }
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

        if (dto == null
                || (StringUtils.isBlank(dto.getUsername())
                && StringUtils.isBlank(dto.getPhone()))) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        User user = userDomainService.getValidUser(userId);

        boolean changed = false;

        if (StringUtils.isNotBlank(dto.getUsername())
                && !dto.getUsername().equals(user.getUsername())) {

            checkUsernameUnique(dto.getUsername(), userId);
            user.setUsername(dto.getUsername());
            changed = true;
        }

        if (StringUtils.isNotBlank(dto.getPhone())
                && !dto.getPhone().equals(user.getPhone())) {

            verifyPhoneCode(
                    dto.getPhone(),
                    dto.getPhoneCode()
            );

            checkPhoneUnique(dto.getPhone(), userId);

            user.setPhone(dto.getPhone());
            changed = true;
        }

        if (!changed) {
            return;
        }

        userMapper.updateById(user);
        userCacheService.evictUserInfo(userId);
    }

    /*
    *  验证手机验证码
    * */
    private void verifyPhoneCode(
            String phone,
            String code
    ) {
        if (StringUtils.isBlank(code)) {
            throw new BizException(CommonErrorCode.PHONE_CODE_REQUIRED);
        }

        boolean valid = phoneCodeCacheService.verifyAndDelete(
                VerifyCodeSceneEnum.BIND_PHONE,
                phone,
                code
        );

        if (!valid) {
            throw new BizException(CommonErrorCode.PHONE_CODE_ERROR);
        }
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
    // TODO:
    // 当手机号已被其他账号绑定时，目前策略为直接拒绝。
    // 后续可扩展为：
    // 1. 提示用户选择是否迁移账号
    // 2. 原账号二次确认 / 注销后释放手机号
    // 3. 风控审核流程（高风险操作）

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

        if (dto == null
                || StringUtils.isAnyBlank(
                dto.getOldPassword(),
                dto.getNewPassword(),
                dto.getConfirmPassword())) {

            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        User user = userDomainService.getValidUser(userId);

        if (!PasswordUtil.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(CommonErrorCode.PASSWORD_ERROR);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(CommonErrorCode.PASSWORD_CONFIRM_ERROR);
        }

        user.setPassword(PasswordUtil.encrypt(dto.getNewPassword()));
        userMapper.updateById(user);

        StpUtil.logout();
    }


    /*
    *  重置密码
    * */
    @Override
    public void updatePasswordByPhone(UserPasswordResetByPhoneDTO  dto) {

        if (dto == null
                || StringUtils.isAnyBlank(
                dto.getPhone(),
                dto.getNewPassword(),
                dto.getConfirmPassword())) {

            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        boolean valid = phoneCodeCacheService.verifyAndDelete(
                VerifyCodeSceneEnum.RESET_PASSWORD,
                dto.getPhone(),
                dto.getPhoneCode()
        );

        if (!valid) {
            throw new BizException(CommonErrorCode.PHONE_CODE_ERROR);
        }


        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BizException(CommonErrorCode.PASSWORD_CONFIRM_ERROR);
        }

        User user = userDomainService.getValidUserByPhone(dto.getPhone());

        user.setPassword(PasswordUtil.encrypt(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    /*
    *  发送手机验证码
    * */
    // TODO: 网关层增加 IP 限流，防止恶意刷验证码
    @Override
    public void sendPhoneVerifyCode(PhoneVerifyCodeSendDTO dto) {

        if (dto == null
                || StringUtils.isBlank(dto.getPhone())
                || dto.getScene() == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        String phone = dto.getPhone();
        VerifyCodeSceneEnum scene = dto.getScene();

        if (!PhoneUtil.isValid(phone)) {
            throw new BizException(CommonErrorCode.PHONE_FORMAT_ERROR);
        }

        boolean allow = phoneCodeLimitCacheService.tryAcquire(scene, phone);
        if (!allow) {
            throw new BizException(CommonErrorCode.PHONE_CODE_TOO_FREQUENT);
        }

        String code = PhoneUtil.generateSixDigit();

        phoneCodeCacheService.saveCode(scene, phone, code);

    }


}
