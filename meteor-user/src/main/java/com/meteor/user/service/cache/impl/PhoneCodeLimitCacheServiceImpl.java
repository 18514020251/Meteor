package com.meteor.user.service.cache.impl;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.enums.VerifyCodeSceneEnum;
import com.meteor.user.service.cache.IPhoneCodeLimitCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.meteor.common.cache.RedisKeyConstants.LIMIT_FLAG;

/**
 *  手机验证码限流缓存服务实现
 *
 * @author Programmer
 * @date 2026-01-20 21:29
 */
@Service
@RequiredArgsConstructor
public class PhoneCodeLimitCacheServiceImpl
        implements IPhoneCodeLimitCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean tryAcquire(VerifyCodeSceneEnum scene, String phone) {

        String key = RedisKeyConstants.phoneCodeLimitKey(
                VerifyCodeSceneEnum.BIND_PHONE,
                phone
        );

        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(
                        key,
                        LIMIT_FLAG,
                        RedisKeyConstants.PHONE_CODE_LIMIT_TTL
                );

        return Boolean.TRUE.equals(success);
    }
}

