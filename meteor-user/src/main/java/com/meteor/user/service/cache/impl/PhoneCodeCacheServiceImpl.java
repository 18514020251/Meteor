package com.meteor.user.service.cache.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.enums.VerifyCodeSceneEnum;
import com.meteor.user.service.cache.IPhoneCodeCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.meteor.common.constants.VerifyCodeConstants.PHONE_CODE_TTL;


/**
 *  手机验证码缓存服务实现类
 *
 * @author Programmer
 * @date 2026-01-20 17:12
 */
@Service
@RequiredArgsConstructor
public class PhoneCodeCacheServiceImpl implements IPhoneCodeCacheService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveCode(VerifyCodeSceneEnum scene, String phone, String code) {
        String key = RedisKeyConstants.phoneCodeKey(scene, phone);
        redisTemplate.opsForValue().set(
                key,
                code,
                PHONE_CODE_TTL
        );
    }

    @Override
    public String getCode(VerifyCodeSceneEnum scene, String phone) {
        return redisTemplate.opsForValue()
                .get(RedisKeyConstants.phoneCodeKey(scene, phone));
    }

    @Override
    public void deleteCode(VerifyCodeSceneEnum scene, String phone) {
        redisTemplate.delete(
                RedisKeyConstants.phoneCodeKey(scene, phone)
        );
    }

    @Override
    public boolean verifyAndDelete(
            VerifyCodeSceneEnum scene,
            String phone,
            String inputCode
    ) {
        if (StringUtils.isBlank(inputCode)) {
            return false;
        }

        String key = RedisKeyConstants.phoneCodeKey(scene, phone);
        String realCode = redisTemplate.opsForValue().get(key);

        if (!inputCode.equals(realCode)) {
            return false;
        }

        redisTemplate.delete(key);
        return true;
    }
}

