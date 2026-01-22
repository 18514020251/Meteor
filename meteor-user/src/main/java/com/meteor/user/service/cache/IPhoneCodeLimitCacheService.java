package com.meteor.user.service.cache;

import com.meteor.common.enums.VerifyCodeSceneEnum;

/**
 *  手机验证码发送频率限制缓存服务
 *
 * @author Programmer
 * @date 2026-01-20 21:28
 */
public interface IPhoneCodeLimitCacheService {

    /**
     * 是否允许发送验证码
     */
    boolean tryAcquire(VerifyCodeSceneEnum scene, String phone);

    boolean tryAcquireByIp(VerifyCodeSceneEnum scene, String ip);

}
