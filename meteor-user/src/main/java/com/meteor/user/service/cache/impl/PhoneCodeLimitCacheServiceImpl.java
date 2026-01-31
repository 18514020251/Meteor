package com.meteor.user.service.cache.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.enums.user.VerifyCodeSceneEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
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

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean tryAcquire(VerifyCodeSceneEnum scene, String phone) {

        if (scene == null || StringUtils.isBlank(phone)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }

        String key = RedisKeyConstants.phoneCodeLimitKey(scene, phone);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(
                        key,
                        LIMIT_FLAG,
                        RedisKeyConstants.PHONE_CODE_LIMIT_TTL
                );

        return Boolean.TRUE.equals(success);
    }


    @Override
    public boolean tryAcquireByIp(VerifyCodeSceneEnum scene, String ip) {
        if (scene == null || StringUtils.isBlank(ip)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID);
        }
        String key = RedisKeyConstants.phoneCodeIpLimitKey(scene, ip);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, RedisKeyConstants.PHONE_CODE_IP_LIMIT_TTL);
        }

        return count != null && count <= RedisKeyConstants.PHONE_CODE_IP_LIMIT_COUNT;

    }

}

