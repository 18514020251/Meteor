package com.meteor.user.service.cache.impl;

import com.meteor.common.enums.VerifyCodeSceneEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.user.service.cache.IPhoneCodeLimitCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

/**
 * 手机验证码限流缓存服务实现测试类
 *
 * @author Programmer
 * @date 2026-01-22
 */
@ExtendWith(MockitoExtension.class)
class PhoneCodeLimitCacheServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private IPhoneCodeLimitCacheService phoneCodeLimitCacheService;

    @BeforeEach
    void setUp() {
        // 模拟 redisTemplate.opsForValue() 返回 valueOperations，使用 lenient() 允许不必要的模拟
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        phoneCodeLimitCacheService = new PhoneCodeLimitCacheServiceImpl(redisTemplate);
    }

    /**
     * 测试 tryAcquireByIp 方法 - scene 为 null 的情况
     */
    @Test
    void tryAcquireByIp_ShouldThrowParamInvalidException_WhenSceneIsNull() {
        VerifyCodeSceneEnum scene = null;
        String ip = "127.0.0.1";

        BizException exception = assertThrows(
                BizException.class,
                () -> phoneCodeLimitCacheService.tryAcquireByIp(scene, ip),
                "scene 为 null 时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PARAM_INVALID.getCode(), exception.getCode(), "异常代码应该匹配 PARAM_INVALID");
    }

    /**
     * 测试 tryAcquireByIp 方法 - ip 为空白的情况
     */
    @Test
    void tryAcquireByIp_ShouldThrowParamInvalidException_WhenIpIsBlank() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String ip = "";

        BizException exception = assertThrows(
                BizException.class,
                () -> phoneCodeLimitCacheService.tryAcquireByIp(scene, ip),
                "ip 为空白时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PARAM_INVALID.getCode(), exception.getCode(), "异常代码应该匹配 PARAM_INVALID");
    }

    /**
     * 测试 tryAcquireByIp 方法 - ip 为 null 的情况
     */
    @Test
    void tryAcquireByIp_ShouldThrowParamInvalidException_WhenIpIsNull() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String ip = null;

        BizException exception = assertThrows(
                BizException.class,
                () -> phoneCodeLimitCacheService.tryAcquireByIp(scene, ip),
                "ip 为 null 时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PARAM_INVALID.getCode(), exception.getCode(), "异常代码应该匹配 PARAM_INVALID");
    }

    /**
     * 测试 tryAcquireByIp 方法 - increment 返回 null 的情况
     */
    @Test
    void tryAcquireByIp_ShouldReturnFalse_WhenIncrementReturnsNull() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String ip = "127.0.0.1";

        lenient().when(valueOperations.increment(anyString())).thenReturn(null);

        boolean result = phoneCodeLimitCacheService.tryAcquireByIp(scene, ip);

        assertFalse(result, "increment 返回 null 时应该返回 false");
    }

    /**
     * 测试 tryAcquireByIp 方法 - increment 返回 1 的情况
     */
    @Test
    void tryAcquireByIp_ShouldReturnTrueAndSetExpire_WhenIncrementReturnsOne() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String ip = "127.0.0.1";

        lenient().when(valueOperations.increment(anyString())).thenReturn(1L);

        boolean result = phoneCodeLimitCacheService.tryAcquireByIp(scene, ip);

        assertTrue(result, "increment 返回 1 时应该返回 true");
        
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    /**
     * 测试 tryAcquireByIp 方法 - increment 返回大于 1 但小于等于限制的情况
     */
    @Test
    void tryAcquireByIp_ShouldReturnTrue_WhenIncrementReturnsWithinLimit() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String ip = "127.0.0.1";

        lenient().when(valueOperations.increment(anyString())).thenReturn(2L);

        boolean result = phoneCodeLimitCacheService.tryAcquireByIp(scene, ip);

        assertTrue(result, "increment 返回在限制范围内的值时应该返回 true");
    }

    /**
     * 测试 tryAcquireByIp 方法 - increment 返回大于限制的情况
     */
    @Test
    void tryAcquireByIp_ShouldReturnFalse_WhenIncrementExceedsLimit() {
        VerifyCodeSceneEnum scene = VerifyCodeSceneEnum.LOGIN;
        String ip = "127.0.0.1";

        lenient().when(valueOperations.increment(anyString())).thenReturn(6L);

        boolean result = phoneCodeLimitCacheService.tryAcquireByIp(scene, ip);

        assertFalse(result, "increment 返回超过限制的值时应该返回 false");
    }
}
