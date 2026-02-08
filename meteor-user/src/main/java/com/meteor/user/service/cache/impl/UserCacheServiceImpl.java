package com.meteor.user.service.cache.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.common.cache.RedisKeyConstants;
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
        String infoKey = buildUserInfoKey(userId);
        String roleKey = buildUserRoleKey(userId);

        String infoValue;
        String roleValue;

        try {
            infoValue = redisTemplate.opsForValue().get(infoKey);
            roleValue = redisTemplate.opsForValue().get(roleKey);
        } catch (Exception e) {
            log.warn("读取用户缓存失败, userId={}", userId, e);
            return null;
        }

        if (infoValue == null || roleValue == null){
            return null;
        }

        try {
            return objectMapper.readValue(infoValue, UserInfoCache.class);
        } catch (Exception e) {
            log.warn("反序列化用户缓存失败, userId={}", userId, e);
            return null;
        }
    }


    @Override
    public boolean isNullCached(Long userId) {
        String key = buildUserInfoKey(userId);

        String value;
        try {
            value = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("读取用户空缓存失败, userId={}", userId, e);
            return false;
        }
        return CACHE_NULL_VALUE.equals(value);
    }

    @Override
    public void cacheUserAll(Long userId, String role , UserInfoCache cache) {
        cacheUserRole(userId , role);
        cacheUserInfo(userId, cache);
    }


    @Override
    public void cacheUserRole(Long userId, String role) {
        String key = buildUserRoleKey(userId);
        try {
            redisTemplate.opsForValue().set(
                    key,
                    role,
                    USER_ROLE_TTL
            );
        } catch (Exception e) {
            log.warn("写入用户缓存失败, userId={}", userId, e);
        }
    }

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

    @Override
    public void evictUserRole(Long userId) {
        String key = buildUserRoleKey(userId);
        redisTemplate.delete(key);
    }

    @Override
    public void evictUserAll(Long userId) {
        evictUserInfo(userId);
        evictUserRole(userId);
    }

    @Override
    public void cacheOnlineUser(Long userId, String token, String ip, String role) {
        String zsetKey = RedisKeyConstants.onlineUserZsetKey();
        String detailKey = RedisKeyConstants.buildOnlineUserDetailKey(userId);

        long now = System.currentTimeMillis();

        try {
            redisTemplate.opsForZSet().add(zsetKey, String.valueOf(userId), now);

            redisTemplate.opsForHash().put(detailKey, RedisKeyConstants.ONLINE_USER_FIELD_TOKEN, token);
            redisTemplate.opsForHash().put(detailKey, RedisKeyConstants.ONLINE_USER_FIELD_IP, ip);
            redisTemplate.opsForHash().put(detailKey, RedisKeyConstants.ONLINE_USER_FIELD_LOGIN_TIME, String.valueOf(now));
            redisTemplate.opsForHash().put(detailKey, RedisKeyConstants.ONLINE_USER_FIELD_ROLE, role);

            redisTemplate.expire(detailKey, RedisKeyConstants.ONLINE_USER_TTL);
        } catch (Exception e) {
            log.warn("写入在线用户缓存失败, userId={}", userId, e);
        }
    }

    @Override
    public void removeOnlineUser(Long userId) {
        String zsetKey = RedisKeyConstants.onlineUserZsetKey();
        String detailKey = RedisKeyConstants.buildOnlineUserDetailKey(userId);
        try {
            redisTemplate.opsForZSet().remove(zsetKey, String.valueOf(userId));
            redisTemplate.delete(detailKey);
        } catch (Exception e) {
            log.warn("删除在线用户缓存失败, userId={}", userId, e);
        }
    }

}


