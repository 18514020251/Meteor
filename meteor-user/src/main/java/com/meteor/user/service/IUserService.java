package com.meteor.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.user.domain.dto.*;
import com.meteor.user.domain.entity.User;
import com.meteor.user.domain.vo.UserInfoVO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Programmer
 * @since 2026-01-13
 */
public interface IUserService extends IService<User> {

    void register(UserRegisterReq req);

    String login(@Valid UserLoginReq req);

    UserInfoVO getCurrentUserInfo(Long userId);

    String uploadAvatar(MultipartFile file , Long userId);

    void deleteUserAndRelatedInfo(Long userId);

    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    void updatePassword(Long userId, @Valid UserPasswordUpdateDTO dto);

    void updatePasswordByPhone(@Valid UserPasswordResetByPhoneDTO dto);

    void sendPhoneVerifyCode(PhoneVerifyCodeSendDTO phone , String clientIp);
}
