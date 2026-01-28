package com.meteor.user.service.cache;

import com.meteor.user.service.cache.model.UserInfoCache;

/**
 * 用户信息缓存服务
 *
 * @author Programmer
 */
public interface IUserCacheService {

    /**
     * 获取用户信息缓存
     *
     * @param userId 用户 ID
     * @return
     *  - 命中：UserInfoVO
     *  - 未命中：null
     */
    UserInfoCache getUserInfo(Long userId);

    /**
     * 判断用户信息缓存是否为空
     *
     * @param userId 用户 ID
     * @return
     *  - 命中：true
     *  - 未命中：false
     */
    boolean isNullCached(Long userId);

    /**
     * 缓存用户角色
     */
    void cacheUserRole(Long userId, String role);

    /**
     * 缓存用户信息
     */
    void cacheUserAll(Long userId , String role , UserInfoCache cache);

    /**
     * 缓存空值（防穿透）
     */
    void cacheNull(Long userId);

    /**
     * 删除用户信息缓存
     */
    void evictUserInfo(Long userId);

    /*
    *  删除用户角色缓存
    * */
    void evictUserRole(Long userId);

    /*
    *  删除用户所有缓存
    * */
    void evictUserAll(Long userId);
    }

