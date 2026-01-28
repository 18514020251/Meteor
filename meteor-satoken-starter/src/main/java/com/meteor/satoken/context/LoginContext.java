package com.meteor.satoken.context;

import cn.dev33.satoken.stp.StpUtil;

/**
 * @author Programmer
 * @date 2026-01-28 13:42
 */
public class LoginContext {

    /**
     * <p>获取当前登录用户ID</p>
     * */
    public Long currentLoginId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * <p>判断当前用户是否登录</p>
     * */
    public boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * <p>检查当前用户是否是管理员</p>
     * */
    public void checkAdminRole() {
        StpUtil.checkRole("admin");
    }

    /**
     * <p>取消用户登录状态</p>
     * */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * <p>根据用户id取消用户登录状态</p>
     * */
    public void kickout(Long userId) {
        StpUtil.logout(userId);
    }

}
