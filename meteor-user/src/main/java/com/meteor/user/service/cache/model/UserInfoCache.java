package com.meteor.user.service.cache.model;

import com.meteor.user.domain.entity.User;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息缓存对象（稳定数据）
 *
 * @author Programmer
 * @date 2026-01-19 21:12
 */

@Data
public class UserInfoCache implements Serializable {

    private Long userId;

    private String username;

    private Integer role;

    private String avatarObject;

    private String phone;

    /**
     * 从 User 构建缓存对象
     */
    public static UserInfoCache fromUser(User user) {
        UserInfoCache cache = new UserInfoCache();
        cache.userId = user.getId();
        cache.username = user.getUsername();
        cache.role = user.getRole();
        cache.avatarObject = user.getAvatar();
        cache.phone = user.getPhone();
        return cache;
    }
}
