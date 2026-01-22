package com.meteor.user.service.cache.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteor.user.service.cache.IUserCacheService;
import com.meteor.user.service.cache.model.UserInfoCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户信息缓存服务实现测试类
 *
 * @author Programmer
 * @date 2026-01-22
 */
@ExtendWith(MockitoExtension.class)
class UserCacheServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    private IUserCacheService userCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        userCacheService = new UserCacheServiceImpl(redisTemplate, objectMapper);
    }

    /**
     * 测试 getUserInfo 方法 - Redis 读取失败的情况
     */
    @Test
    void getUserInfo_ShouldReturnNull_WhenRedisReadFails(){
        Long userId = 1L;
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis read error"));
        UserInfoCache result = userCacheService.getUserInfo(userId);
        assertNull(result);
    }

    /**
     * 测试 getUserInfo 方法 - 缓存值为 null 的情况
     */
    @Test
    void getUserInfo_ShouldReturnNull_WhenValueIsNull(){
        Long userId = 1L;
        when(valueOperations.get(anyString())).thenReturn(null);
        UserInfoCache result = userCacheService.getUserInfo(userId);
        assertNull(result);
    }

    /**
     * 测试 getUserInfo 方法 - 缓存值为 CACHE_NULL_VALUE 的情况
     */
    @Test
    void getUserInfo_ShouldReturnNull_WhenValueIsCacheNullValue(){
        Long userId = 1L;
        when(valueOperations.get(anyString())).thenReturn("null");
        UserInfoCache result = userCacheService.getUserInfo(userId);
        assertNull(result);
    }

    /**
     * 测试 getUserInfo 方法 - 反序列化失败的情况
     */
    @Test
    void getUserInfo_ShouldReturnNull_WhenDeserializationFails() throws Exception {
        Long userId = 1L;
        String cachedValue = "invalid json";
        when(valueOperations.get(anyString())).thenReturn(cachedValue);
        when(objectMapper.readValue(anyString(), eq(UserInfoCache.class))).thenThrow(new RuntimeException("Deserialization error"));
        UserInfoCache result = userCacheService.getUserInfo(userId);
        assertNull(result);
    }

    /**
     * 测试 getUserInfo 方法 - 正常情况，成功获取用户信息的情况
     */
    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenEverythingIsNormal() throws Exception {
        Long userId = 1L;
        String cachedValue = "{\"userId\": 1, \"username\": \"test\"}";
        UserInfoCache expectedUserInfo = new UserInfoCache();
        expectedUserInfo.setUserId(userId);
        expectedUserInfo.setUsername("test");
        when(valueOperations.get(anyString())).thenReturn(cachedValue);
        when(objectMapper.readValue(anyString(), eq(UserInfoCache.class))).thenReturn(expectedUserInfo);
        UserInfoCache result = userCacheService.getUserInfo(userId);
        assertNotNull(result);
        assertEquals(expectedUserInfo.getUserId(), result.getUserId());
        assertEquals(expectedUserInfo.getUsername(), result.getUsername());
    }

    /**
     * 测试 cacheNull 方法 - 正常情况，写入缓存成功
     */
    @Test
    void cacheNull_ShouldWriteNullValueToCache_WhenEverythingIsNormal() {
        Long userId = 1L;
        userCacheService.cacheNull(userId);
        // 验证调用了 Redis set 操作
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    /**
     * 测试 cacheNull 方法 - 异常情况，写入缓存失败
     */
    @Test
    void cacheNull_ShouldNotThrowException_WhenRedisWriteFails() {
        Long userId = 1L;
        doThrow(new RuntimeException("Redis write error")).when(valueOperations).set(anyString(), anyString(), anyLong(), any());
        // 执行测试，应该正常返回，不会抛出异常
        assertDoesNotThrow(() -> userCacheService.cacheNull(userId), "写入缓存失败时不应该抛出异常");
    }
}
