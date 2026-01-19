package com.meteor.user.service.cache.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.utils.RedisTtlUtil;
import com.meteor.user.service.cache.IUserCacheService;
import com.meteor.user.service.cache.model.UserInfoCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.meteor.common.cache.RedisKeyConstants.*;

/**
 * 用户信息缓存服务实现
 *
 * @author Programmer
 * @date 2026-01-18 10:22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheServiceImpl implements IUserCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public UserInfoCache getUserInfo(Long userId) {
        String key = buildUserInfoKey(userId);

        String value;
        try {
            value = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("读取用户缓存失败, userId={}", userId, e);
            return null;
        }

        if (value == null || CACHE_NULL_VALUE.equals(value)) {
            return null;
        }

        try {
            return objectMapper.readValue(value, UserInfoCache.class);
        } catch (Exception e) {
            log.warn("反序列化用户缓存失败, userId={}", userId, e);
            return null;
        }
    }

    @Override
    public void cacheUserInfo(Long userId, UserInfoCache cache) {
        String key = buildUserInfoKey(userId);
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(cache),
                    RedisTtlUtil.withRandom(USER_INFO_TTL, USER_INFO_TTL_RANDOM),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.warn("写入用户缓存失败, userId={}", userId, e);
        }
    }

    @Override
    public void cacheNull(Long userId) {
        String key = buildUserInfoKey(userId);
        try {
            redisTemplate.opsForValue().set(
                    key,
                    CACHE_NULL_VALUE,
                    RedisTtlUtil.toSeconds(USER_INFO_NULL_TTL),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.warn("写入用户空缓存失败, userId={}", userId, e);
        }
    }

    @Override
    public void evictUserInfo(Long userId) {
        String key = buildUserInfoKey(userId);
        redisTemplate.delete(key);
    }

    private String buildUserInfoKey(Long userId) {
        return String.format(USER_INFO_KEY, userId);
    }
}


