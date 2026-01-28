package com.meteor.satoken.context;

import cn.dev33.satoken.stp.StpUtil;
import com.meteor.satoken.constants.RoleConst;

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

    /**
     * <p>检查当前用户是否是普通用户</p>
     */
    public void checkUserRole(){
        StpUtil.checkRole(RoleConst.USER);
    }

    /**
     * <p>检查当前用户是否是管理员</p>
     * */
    public void checkAdminRole() {
        StpUtil.checkRole(RoleConst.ADMIN);
    }
}
