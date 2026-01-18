package com.meteor.user.service.cache;

import com.meteor.user.domain.vo.UserInfoVO;

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
    UserInfoVO getUserInfo(Long userId);

    /**
     * 缓存用户信息
     */
    void cacheUserInfo(Long userId, UserInfoVO userInfo);

    /**
     * 缓存空值（防缓存穿透）
     */
    void cacheNull(Long userId);

    /**
     * 删除用户信息缓存
     */
    void evictUserInfo(Long userId);
}
