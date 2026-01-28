package com.meteor.satoken.context;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * @author Programmer
 * @date 2026-01-28 13:42
 */
@Component
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
}
