package com.meteor.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.exception.BizException;
import com.meteor.user.domain.dto.UserLoginReq;
import com.meteor.user.domain.dto.UserRegisterReq;
import com.meteor.user.domain.entiey.User;
import com.meteor.common.enums.DeleteStatus;
import com.meteor.user.enums.RoleEnum;
import com.meteor.user.enums.UserStatus;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.service.IUserService;
import com.meteor.common.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;

    /*
    * 注册
    * */
    @Override
    public void register(UserRegisterReq req) {

        // todo： 验证码的获取/判断/

        registerNonEmpty(req);

        boolean exists = exists(req.getUsername());

        if (exists) {
            throw new BizException("用户已存在");
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
            throw new BizException("用户名或密码不能为空");
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
            throw new BizException("用户名或密码错误");
        }

        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        if (!user.isNormal()) {
            throw new BizException("账号不可用");
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

}
