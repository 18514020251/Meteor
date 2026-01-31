package com.meteor.user.service.cache;

import com.meteor.common.enums.user.VerifyCodeSceneEnum;

/**
 * 手机验证码缓存服务接口。
 * <p>
 * 用于对手机验证码进行缓存、获取、删除及校验操作，
 * 支持按业务场景区分验证码数据，通常基于 Redis 等缓存实现。
 * </p>
 *
 * @author Programmer
 * @date 2026-01-20 17:12
 */
public interface IPhoneCodeCacheService {

    /**
     * 保存手机验证码到缓存中。
     * <p>
     * 验证码通常具有有效期，具体过期时间由实现类控制。
     * </p>
     *
     * @param scene 验证码业务场景
     * @param phone 手机号
     * @param code 验证码内容
     */
    void saveCode(VerifyCodeSceneEnum scene, String phone, String code);

    /**
     * 从缓存中获取手机验证码。
     * <p>
     * 若验证码不存在或已过期，返回 {@code null}。
     * </p>
     *
     * @param scene 验证码业务场景
     * @param phone 手机号
     * @return 缓存中的验证码内容，不存在时返回 {@code null}
     */
    String getCode(VerifyCodeSceneEnum scene, String phone);

    /**
     * 从缓存中删除指定手机号的验证码。
     * <p>
     * 通常在验证码校验成功或业务终止时调用。
     * </p>
     *
     * @param scene 验证码业务场景
     * @param phone 手机号
     */
    void deleteCode(VerifyCodeSceneEnum scene, String phone);

    /**
     * 校验验证码并在校验完成后删除缓存中的验证码。
     * <p>
     * 该方法为验证码校验的核心逻辑，
     * 会对输入验证码与缓存中的验证码进行比对，
     * 无论校验成功或失败，均会清理对应的缓存数据，
     * 以防止验证码被重复使用。
     * </p>
     *
     * @param scene 验证码业务场景
     * @param phone 手机号
     * @param inputCode 用户输入的验证码
     * @return {@code true} 表示验证码校验成功，
     *         {@code false} 表示验证码校验失败或已过期
     */
    boolean verifyAndDelete(
            VerifyCodeSceneEnum scene,
            String phone,
            String inputCode
    );
}
