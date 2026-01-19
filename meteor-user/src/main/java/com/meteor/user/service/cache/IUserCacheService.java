package com.meteor.user.service.cache;

import com.meteor.user.domain.vo.UserInfoVO;
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
     * 缓存用户信息
     */
    void cacheUserInfo(Long userId, UserInfoCache cache);

    /**
     * 缓存空值（防穿透）
     */
    void cacheNull(Long userId);

    /**
     * 删除用户信息缓存（头像 / 信息更新时）
     */
    void evictUserInfo(Long userId);
    }

