package com.meteor.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.common.dto.UserProfileDTO;
import com.meteor.user.controller.dto.*;
import com.meteor.user.controller.vo.UserLoginVO;
import com.meteor.user.domain.entity.User;
import com.meteor.user.controller.vo.UserInfoVO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口。
 *
 * <p>封装用户注册、登录、资料维护、密码更新、验证码发送等核心能力。</p>
 *
 * @author Programmer
 * @since 2026-01-13
 */
public interface IUserService extends IService<User> {

    /**
     * 用户注册。
     *
     * @param req 注册请求参数（用户名、密码等）
     * @throws com.meteor.common.exception.BizException 当参数不合法、用户名/手机号已存在等业务异常时抛出
     */
    void register(@Valid UserRegisterReq req);

    /**
     * 用户登录。
     *
     * @param req 登录请求参数（用户名、密码）
     * @return 用户id 、 用户名 、 用户身份 、 用户是否首次登录
     * @throws com.meteor.common.exception.BizException 当账号不存在、密码错误、账号状态异常等业务异常时抛出
     */
    UserLoginVO login(@Valid UserLoginReq req);

    /**
     * 获取当前登录用户的个人信息。
     *
     * @param userId 当前用户 ID
     * @return 用户信息视图对象
     * @throws com.meteor.common.exception.BizException 当用户不存在或无权限访问时抛出
     */
    UserInfoVO getCurrentUserInfo(Long userId);

    /**
     * 上传并更新用户头像。
     *
     * @param file   头像文件
     * @param userId 当前用户 ID
     * @return 头像访问 URL（或存储路径）
     * @throws com.meteor.common.exception.BizException 当文件为空/格式不合法/上传失败等业务异常时抛出
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 删除用户账号及其关联信息（逻辑删除/级联清理视具体实现）。
     *
     * @param userId 当前用户 ID
     * @throws com.meteor.common.exception.BizException 当用户不存在、删除失败等业务异常时抛出
     */
    void deleteUserAndRelatedInfo(Long userId);

    /**
     * 修改用户资料（如用户名、手机号等）。
     *
     * @param userId 当前用户 ID
     * @param dto    用户资料更新参数
     * @throws com.meteor.common.exception.BizException 当参数不合法、手机号/用户名冲突等业务异常时抛出
     */
    void updateProfile(Long userId, @Valid UserProfileUpdateDTO dto);

    /**
     * 修改用户密码（需登录）。
     *
     * @param userId 当前用户 ID
     * @param dto    密码更新参数（旧密码/新密码等）
     * @throws com.meteor.common.exception.BizException 当旧密码错误、密码不符合规则等业务异常时抛出
     */
    void updatePassword(Long userId, @Valid UserPasswordUpdateDTO dto);

    /**
     * 通过手机号重置用户密码（无需登录，通常依赖验证码校验）。
     *
     * @param dto 手机号重置密码参数
     * @throws com.meteor.common.exception.BizException 当手机号不存在、验证码错误/过期等业务异常时抛出
     */
    void updatePasswordByPhone(@Valid UserPasswordResetByPhoneDTO dto);

    /**
     * 发送手机验证码。
     *
     * @param phone    发送验证码请求参数（手机号、场景等）
     * @param clientIp 客户端 IP（用于风控/限流）
     * @throws com.meteor.common.exception.BizException 当触发限流、手机号不合法、发送失败等业务异常时抛出
     */
    void sendPhoneVerifyCode(@Valid PhoneVerifyCodeSendDTO phone, String clientIp);

    /**
     *  内部接口 用于远程调用
     *  merchant 模块 /me接口 -> user模块
     *
     * @param userId 用户Id
     * @return 用户信息DTO
     * */
    UserProfileDTO getUserProfile(Long userId);
}
