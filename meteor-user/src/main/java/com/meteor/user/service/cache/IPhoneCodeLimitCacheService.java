package com.meteor.user.service.cache;

import com.meteor.common.enums.user.VerifyCodeSceneEnum;

/**
 * 手机验证码发送频率限制缓存服务接口。
 * <p>
 * 用于控制验证码在不同业务场景下的发送频率，
 * 防止同一手机号或同一 IP 在短时间内频繁请求验证码。
 * </p>
 *
 * @author Programmer
 * @date 2026-01-20 21:28
 */
public interface IPhoneCodeLimitCacheService {

    /**
     * 根据业务场景和手机号尝试获取验证码发送许可。
     * <p>
     * 该方法通常用于限制同一手机号在指定业务场景下的
     * 验证码发送频率。
     * </p>
     *
     * @param scene 验证码业务场景
     * @param phone 手机号
     * @return {@code true} 表示允许发送验证码，
     *         {@code false} 表示已达到频率限制
     */
    boolean tryAcquire(VerifyCodeSceneEnum scene, String phone);

    /**
     * 根据业务场景和 IP 地址尝试获取验证码发送许可。
     * <p>
     * 该方法通常用于限制同一 IP 在指定业务场景下的
     * 验证码发送频率，以防止恶意刷码行为。
     * </p>
     *
     * @param scene 验证码业务场景
     * @param ip 请求方 IP 地址
     * @return {@code true} 表示允许发送验证码，
     *         {@code false} 表示已达到频率限制
     */
    boolean tryAcquireByIp(VerifyCodeSceneEnum scene, String ip);

}
