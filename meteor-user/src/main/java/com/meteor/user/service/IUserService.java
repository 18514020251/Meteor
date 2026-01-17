package com.meteor.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.meteor.user.domain.dto.UserLoginReq;
import com.meteor.user.domain.dto.UserRegisterReq;
import com.meteor.user.domain.entiey.User;
import com.meteor.user.domain.vo.UserInfoVO;
import jakarta.validation.Valid;

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

    UserInfoVO getCurrentUserInfo();
}
