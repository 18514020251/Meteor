package com.meteor.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.constants.AvatarConstants;
import com.meteor.common.dto.UserProfileDTO;
import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.common.enums.user.VerifyCodeSceneEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.utils.PasswordUtil;
import com.meteor.common.utils.PhoneUtil;
import com.meteor.common.utils.image.ImageCropUtil;
import com.meteor.minio.enums.MinioPathEnum;
import com.meteor.minio.util.MinioUtil;
import com.meteor.satoken.constants.RoleConst;
import com.meteor.user.controller.dto.*;
import com.meteor.user.controller.vo.UserLoginVO;
import com.meteor.user.service.assembler.UserInfoAssembler;
import com.meteor.user.domain.entity.User;
import com.meteor.user.controller.vo.UserInfoVO;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.mq.publisher.MerchantApplyEventPublisher;
import com.meteor.user.mq.publisher.MessageApplyEventPublisher;
import com.meteor.user.service.IUserService;
import com.meteor.user.service.cache.IPhoneCodeCacheService;
import com.meteor.user.service.cache.IPhoneCodeLimitCacheService;
import com.meteor.user.service.cache.IUserCacheService;
import com.meteor.user.service.cache.model.UserInfoCache;
import com.meteor.user.service.internal.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.meteor.common.constants.AvatarConstants.ALLOWED_TYPES;
import static com.meteor.common.constants.AvatarConstants.MAX_SIZE;

/**
 * 用户服务实现类
 *
 * <p>负责用户注册、登录、信息查询、头像上传、资料修改、密码修改/重置、验证码发送等核心业务。</p>
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
    private final UserInfoAssembler userInfoAssembler;
    private final MerchantApplyEventPublisher eventPublisher;
    private final MessageApplyEventPublisher messageApplyEventPublisher;

    /**
     * 用户注册
     *
     * <p>校验入参并检查用户名唯一性，创建用户并落库。</p>
     *
     * @param req 注册请求
     * @throws BizException 参数不合法或用户已存在
     */
    @Override
    public void register(UserRegisterReq req) {

        if (req == null
                || StringUtils.isBlank(req.getUsername())
                || StringUtils.isBlank(req.getPassword())) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        User existingUser = userMapper.selectByUsername(req.getUsername());
        if (existingUser != null) {
            throw new BizException(CommonErrorCode.USER_EXIST);
        }

        User user = User.createRegisterUser(
                req.getUsername(),
                req.getPassword()
        );

        userMapper.insert(user);
    }

    /**
     * 用户登录
     *
     * <p>校验用户名密码后，按用户角色写入角色缓存，并使用 Sa-Token 生成 Token。</p>
     *
     * @param req 登录请求
     * @return token
     * @throws BizException 用户名或密码错误
     */
    @Override
    public UserLoginVO login(UserLoginReq req) {

        User user = userDomainService.getNormalUserByUsername(req.getUsername());

        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR);
        }

        String role = switch (user.getRole()) {
            case 2 -> RoleConst.ADMIN;
            case 1 -> RoleConst.MERCHANT;
            default -> RoleConst.USER;
        };

        userCacheService.cacheUserRole(user.getId(), role);

        StpUtil.login(user.getId());
        return userInfoAssembler.toLoginVo(StpUtil.getTokenValue(),user);
    }

    /**
     * 获取当前登录用户信息
     *
     * <p>优先读取缓存；缓存未命中时查库并回填缓存。</p>
     *
     * @param userId 用户 ID
     * @return 用户信息视图
     * @throws BizException 用户不存在
     */
    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {

        if (userCacheService.isNullCached(userId)) {
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }

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

        userCacheService.cacheUserAll(
                userId,
                Objects.requireNonNull(RoleEnum.fromCode(user.getRole())).toString(),
                newCache
        );

        return userInfoAssembler.toVO(newCache);
    }

    /**
     * 上传并更新用户头像
     *
     * <p>进行格式/大小校验后，将头像裁剪为正方形并上传到 MinIO；随后更新用户头像字段并清理缓存。</p>
     *
     * @param file   头像文件
     * @param userId 用户 ID
     * @return 头像可访问 URL（预签名）
     * @throws BizException 文件不合法或图片处理失败
     */
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
        userCacheService.evictUserAll(userId);

        return minioUtil.buildPresignedUrl(objectName);
    }

    /**
     * 获取图片格式（用于图片处理/裁剪工具）
     *
     * @param file 上传文件
     * @return 图片格式字符串（png/jpg）
     * @throws BizException 文件类型不支持
     */
    private String getImageFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BizException(CommonErrorCode.AVATAR_TYPE_ERROR);
        }

        if (AvatarConstants.IMAGE_PNG.equals(contentType)) {
            return AvatarConstants.FORMAT_PNG;
        }

        if (AvatarConstants.IMAGE_JPEG.equals(contentType)
                || AvatarConstants.IMAGE_JPG.equals(contentType)) {
            return AvatarConstants.FORMAT_JPG;
        }

        throw new BizException(CommonErrorCode.AVATAR_TYPE_ERROR);
    }

    /**
     * 校验头像文件
     *
     * <p>校验文件大小与 MIME 类型。</p>
     *
     * @param file 头像文件
     * @throws BizException 文件大小或类型不合法
     */
    private void validateAvatar(MultipartFile file) {

        if (file.getSize() > MAX_SIZE) {
            throw new BizException(CommonErrorCode.AVATAR_SIZE_ERROR);
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BizException(CommonErrorCode.AVATAR_TYPE_ERROR);
        }
    }

    /**
     * 更新用户头像字段，并删除旧头像对象（若不是默认头像）
     *
     * @param userId    用户 ID
     * @param newAvatar 新头像对象名
     */
    private void updateUserAvatar(Long userId, String newAvatar) {
        User oldUser = userMapper.selectById(userId);

        User update = new User();
        update.setId(userId);
        update.setAvatar(newAvatar);
        userMapper.updateById(update);

        deleteUserAvatar(oldUser);
    }

    /**
     * 删除用户及其关联信息
     *
     * <p>注销登录态、清理缓存、删除用户记录，并删除用户头像对象。</p>
     *
     * @param userId 用户 ID
     */
    @Override
    public void deleteUserAndRelatedInfo(Long userId) {
        User user = userMapper.selectById(userId);

        StpUtil.logout();
        userCacheService.evictUserAll(userId);

        userMapper.deleteById(userId);
        deleteUserAvatar(user);
    }

    /**
     * 删除用户头像对象（非默认头像才删除）
     *
     * @param user 用户实体（含头像字段）
     */
    private void deleteUserAvatar(User user) {
        if (user.getAvatar() != null && !user.getAvatar().equals(AvatarConstants.DEFAULT_AVATAR)) {
            minioUtil.delete(user.getAvatar());
        }
    }

    /**
     * 更新用户基础资料
     *
     * <p>支持修改用户名和手机号（手机号修改需校验验证码）。变更成功后清理用户缓存。</p>
     *
     * @param userId 用户 ID
     * @param dto    更新资料 DTO
     * @throws BizException 参数不合法、验证码错误、用户名/手机号不唯一等
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {

        if (dto == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        boolean usernameBlank = StringUtils.isBlank(dto.getUsername());
        boolean phoneBlank = StringUtils.isBlank(dto.getPhone());

        if (usernameBlank && phoneBlank) {
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

            verifyPhoneCode(dto.getPhone(), dto.getPhoneCode());

            checkPhoneUnique(dto.getPhone(), userId);

            user.setPhone(dto.getPhone());
            changed = true;
        }

        if (!changed) {
            return;
        }

        userMapper.updateById(user);
        userCacheService.evictUserAll(userId);
    }

    /**
     * 校验绑定手机号场景的验证码
     *
     * <p>校验通过后会删除验证码（一次性）。</p>
     *
     * @param phone 手机号
     * @param code  验证码
     * @throws BizException 验证码为空或验证码错误
     */
    private void verifyPhoneCode(String phone, String code) {
        if (StringUtils.isBlank(code)) {
            throw new BizException(CommonErrorCode.PHONE_CODE_REQUIRED);
        }
        boolean valid = phoneCodeCacheService.verifyAndDelete(VerifyCodeSceneEnum.BIND_PHONE, phone, code);
        if (!valid) {
            throw new BizException(CommonErrorCode.PHONE_CODE_ERROR);
        }
    }

    /**
     * 检查用户名唯一性（排除当前用户）
     *
     * @param username 新用户名
     * @param userId   当前用户 ID
     * @throws BizException 用户名已存在
     */
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

    /**
     * 校验手机号唯一性（排除当前用户）
     *
     * <p>当前版本策略：</p>
     * <ul>
     *   <li>手机号与账号一对一绑定</li>
     *   <li>已被其他账号占用时直接拒绝</li>
     * </ul>
     *
     * @param phone  新手机号
     * @param userId 当前用户 ID
     * @throws BizException 手机号已存在
     */
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

    /**
     * 修改密码（已登录场景）
     *
     * <p>校验旧密码与新密码一致性，更新成功后强制登出。</p>
     *
     * @param userId 用户 ID
     * @param dto    修改密码 DTO
     * @throws BizException 参数不合法、旧密码错误、新密码确认不一致
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        try {
            messageApplyEventPublisher.publishPasswordChanged(user);
        } catch (Exception e) {
            log.warn("publishPasswordChanged failed, userId={}, err={}", userId, e, e);
        }
    }

    /**
     * 通过手机号重置密码（未登录场景）
     *
     * <p>校验验证码、校验新密码确认一致性后更新密码。</p>
     *
     * @param dto 重置密码 DTO
     * @throws BizException 参数不合法、验证码错误、新密码确认不一致、用户不存在
     */
    @Override
    public void updatePasswordByPhone(UserPasswordResetByPhoneDTO dto) {

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

    /**
     * 发送手机验证码
     *
     * <p>包含手机号格式校验、IP 级别限流、手机号级别限流，并将验证码写入缓存。</p>
     *
     * @param dto      发送验证码 DTO（包含场景）
     * @param clientIp 客户端 IP
     * @throws BizException 参数不合法、请求过于频繁、手机号格式错误
     */
    @Override
    public void sendPhoneVerifyCode(PhoneVerifyCodeSendDTO dto, String clientIp) {

        if (dto == null
                || StringUtils.isBlank(dto.getPhone())
                || dto.getScene() == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        VerifyCodeSceneEnum scene = dto.getScene();

        boolean ipAllow = phoneCodeLimitCacheService.tryAcquireByIp(scene, clientIp);
        if (!ipAllow) {
            throw new BizException(CommonErrorCode.REQUEST_TOO_FREQUENT);
        }

        String phone = dto.getPhone();

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

    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        User user = getById(userId);
        if (user == null || user.getIsDeleted() == 1 || !user.getRole().equals(RoleEnum.MERCHANT.getCode())) {
            throw new BizException(CommonErrorCode.USER_NOT_EXIST);
        }
        return userInfoAssembler.toProfile(user);
    }
}
