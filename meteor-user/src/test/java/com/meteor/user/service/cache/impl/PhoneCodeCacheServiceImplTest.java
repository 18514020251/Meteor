package com.meteor.user.service.cache.impl;

import com.meteor.common.enums.VerifyCodeSceneEnum;
import com.meteor.user.service.cache.IPhoneCodeCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 手机验证码缓存服务实现测试类
 *
 * @author Programmer
 * @date 2026-01-22
 */
@ExtendWith(MockitoExtension.class)
class PhoneCodeCacheServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private IPhoneCodeCacheService phoneCodeCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        phoneCodeCacheService = new PhoneCodeCacheServiceImpl(redisTemplate);
    }

    /**
     * 测试 verifyAndDelete 方法 - 输入验证码为空的情况
     */
    @Test
    void verifyAndDelete_ShouldReturnFalse_WhenInputCodeIsBlank() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";
        String inputCode = "";

        boolean result = phoneCodeCacheService.verifyAndDelete(scene, phone, inputCode);

        assertFalse(result, "输入验证码为空时应该返回 false");
        
        verifyNoInteractions(valueOperations);
    }

    /**
     * 测试 verifyAndDelete 方法 - 输入验证码为 null 的情况
     */
    @Test
    void verifyAndDelete_ShouldReturnFalse_WhenInputCodeIsNull() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";
        String inputCode = null;

        boolean result = phoneCodeCacheService.verifyAndDelete(scene, phone, inputCode);

        assertFalse(result, "输入验证码为 null 时应该返回 false");
        
        verifyNoInteractions(valueOperations);
    }

    /**
     * 测试 verifyAndDelete 方法 - 输入验证码与真实验证码不一致的情况
     */
    @Test
    void verifyAndDelete_ShouldReturnFalse_WhenInputCodeDoesNotMatch() {
        // 准备测试数据
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";
        String inputCode = "123456";
        String realCode = "654321";

        // 模拟 Redis 操作
        when(valueOperations.get(anyString())).thenReturn(realCode);

        // 执行测试
        boolean result = phoneCodeCacheService.verifyAndDelete(scene, phone, inputCode);

        // 验证结果
        assertFalse(result, "输入验证码与真实验证码不一致时应该返回 false");
        
        // 验证调用了 Redis get 操作
        verify(valueOperations).get(anyString());
    }

    /**
     * 测试 verifyAndDelete 方法 - Redis 中没有验证码的情况
     */
    @Test
    void verifyAndDelete_ShouldReturnFalse_WhenRealCodeIsNull() {
        // 准备测试数据
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";
        String inputCode = "123456";

        // 模拟 Redis 操作，返回 null 表示没有验证码
        when(valueOperations.get(anyString())).thenReturn(null);

        // 执行测试
        boolean result = phoneCodeCacheService.verifyAndDelete(scene, phone, inputCode);

        // 验证结果
        assertFalse(result, "Redis 中没有验证码时应该返回 false");
        
        // 验证调用了 Redis get 操作
        verify(valueOperations).get(anyString());
    }

    /**
     * 测试 verifyAndDelete 方法 - 输入验证码与真实验证码一致的情况
     */
    @Test
    void verifyAndDelete_ShouldReturnTrue_WhenInputCodeMatches() {
        // 准备测试数据
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";
        String inputCode = "123456";
        String realCode = "123456";

        // 模拟 Redis 操作
        when(valueOperations.get(anyString())).thenReturn(realCode);

        // 执行测试
        boolean result = phoneCodeCacheService.verifyAndDelete(scene, phone, inputCode);

        // 验证结果
        assertTrue(result, "输入验证码与真实验证码一致时应该返回 true");
        
        verify(valueOperations).get(anyString());
        verify(redisTemplate).delete(anyString());
    }

    /**
     * 测试 getCode 方法 - 正常情况
     */
    @Test
    void getCode_ShouldReturnCode_WhenCodeExists() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";
        String expectedCode = "123456";

        when(valueOperations.get(anyString())).thenReturn(expectedCode);

        String result = phoneCodeCacheService.getCode(scene, phone);

        assertEquals(expectedCode, result, "应该返回正确的验证码");
        
        verify(valueOperations).get(anyString());
    }

    /**
     * 测试 getCode 方法 - 验证码不存在的情况
     */
    @Test
    void getCode_ShouldReturnNull_WhenCodeDoesNotExist() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";

        when(valueOperations.get(anyString())).thenReturn(null);

        String result = phoneCodeCacheService.getCode(scene, phone);

        assertNull(result, "验证码不存在时应该返回 null");
        
        verify(valueOperations).get(anyString());
    }

    /**
     * 测试 deleteCode 方法
     */
    @Test
    void deleteCode_ShouldDeleteCode() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String phone = "13800138000";

        phoneCodeCacheService.deleteCode(scene, phone);

        verify(redisTemplate).delete(anyString());
    }
}
