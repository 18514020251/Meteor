package com.meteor.user.service.cache;

import com.meteor.common.enums.VerifyCodeSceneEnum;

/**
 *  手机验证码缓存服务
 *
 * @author Programmer
 * @date 2026-01-20 17:12
 */
public interface IPhoneCodeCacheService {

    void saveCode(VerifyCodeSceneEnum scene, String phone, String code);

    String getCode(VerifyCodeSceneEnum scene, String phone);

    void deleteCode(VerifyCodeSceneEnum scene, String phone);

    /**
     * 校验验证码并删除（核心方法）
     *
     * @return true 校验成功，false 校验失败或已过期
     */
    boolean verifyAndDelete(
            VerifyCodeSceneEnum scene,
            String phone,
            String inputCode
    );
}

