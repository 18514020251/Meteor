package com.meteor.user.service.impl;

import com.meteor.common.enums.VerifyCodeSceneEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.user.domain.assembler.UserInfoAssembler;
import com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO;
import com.meteor.user.service.cache.IPhoneCodeCacheService;
import com.meteor.user.service.cache.IPhoneCodeLimitCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务实现测试类 - 发送手机验证码方法
 *
 * @author Programmer
 * @date 2026-01-22
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplSendPhoneVerifyCodeTest {

    @Mock
    private IPhoneCodeLimitCacheService phoneCodeLimitCacheService;

    @Mock
    private IPhoneCodeCacheService phoneCodeCacheService;

    private UserServiceImpl userService;

    @Mock
    private UserInfoAssembler userInfoAssembler;

    @BeforeEach
    void setUp() {
        // 使用最小依赖构造 UserServiceImpl
        userService = new UserServiceImpl(
                null, // UserMapper
                null, // MinioUtil
                null, // UserDomainService
                null, // IUserCacheService
                phoneCodeCacheService,
                phoneCodeLimitCacheService,
                userInfoAssembler
        );
    }

    /**
     * 测试 sendPhoneVerifyCode 方法 - dto 为 null 的情况
     */
    @Test
    void sendPhoneVerifyCode_ShouldThrowParamInvalidException_WhenDtoIsNull() {
        PhoneVerifyCodeSendDTO dto = null;
        String clientIp = "127.0.0.1";

        BizException exception = assertThrows(
                BizException.class,
                () -> userService.sendPhoneVerifyCode(dto, clientIp),
                "dto 为 null 时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PARAM_INVALID.getCode(), exception.getCode());
    }

    /**
     * 测试 sendPhoneVerifyCode 方法 - phone 为空白的情况
     */
    @Test
    void sendPhoneVerifyCode_ShouldThrowParamInvalidException_WhenPhoneIsBlank() {
        PhoneVerifyCodeSendDTO dto = new PhoneVerifyCodeSendDTO();
        dto.setPhone("");
        dto.setScene(VerifyCodeSceneEnum.LOGIN);
        String clientIp = "127.0.0.1";

        BizException exception = assertThrows(
                BizException.class,
                () -> userService.sendPhoneVerifyCode(dto, clientIp),
                "phone 为空白时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PARAM_INVALID.getCode(), exception.getCode());
    }

    /**
     * 测试 sendPhoneVerifyCode 方法 - scene 为 null 的情况
     */
    @Test
    void sendPhoneVerifyCode_ShouldThrowParamInvalidException_WhenSceneIsNull() {
        PhoneVerifyCodeSendDTO dto = new PhoneVerifyCodeSendDTO();
        dto.setPhone("13800138000");
        dto.setScene(null);
        String clientIp = "127.0.0.1";

        BizException exception = assertThrows(
                BizException.class,
                () -> userService.sendPhoneVerifyCode(dto, clientIp),
                "scene 为 null 时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PARAM_INVALID.getCode(), exception.getCode());
    }

    /**
     * 测试 sendPhoneVerifyCode 方法 - IP 限流的情况
     */
    @Test
    void sendPhoneVerifyCode_ShouldThrowRequestTooFrequentException_WhenIpNotAllowed() {
        PhoneVerifyCodeSendDTO dto = new PhoneVerifyCodeSendDTO();
        dto.setPhone("13800138000");
        dto.setScene(VerifyCodeSceneEnum.LOGIN);
        String clientIp = "127.0.0.1";

        when(phoneCodeLimitCacheService.tryAcquireByIp(any(), anyString())).thenReturn(false);

        BizException exception = assertThrows(
                BizException.class,
                () -> userService.sendPhoneVerifyCode(dto, clientIp),
                "IP 限流时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.REQUEST_TOO_FREQUENT.getCode(), exception.getCode());
    }

    /**
     * 测试 sendPhoneVerifyCode 方法 - 手机号格式错误的情况
     */
    @Test
    void sendPhoneVerifyCode_ShouldThrowPhoneFormatErrorException_WhenPhoneIsInvalid() {
        PhoneVerifyCodeSendDTO dto = new PhoneVerifyCodeSendDTO();
        dto.setPhone("123456"); // 无效的手机号
        dto.setScene(VerifyCodeSceneEnum.LOGIN);
        String clientIp = "127.0.0.1";

        when(phoneCodeLimitCacheService.tryAcquireByIp(any(), anyString())).thenReturn(true);

        BizException exception = assertThrows(
                BizException.class,
                () -> userService.sendPhoneVerifyCode(dto, clientIp),
                "手机号格式错误时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PHONE_FORMAT_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试 sendPhoneVerifyCode 方法 - 手机号发送频率过高的情况
     */
    @Test
    void sendPhoneVerifyCode_ShouldThrowPhoneCodeTooFrequentException_WhenPhoneNotAllowed() {
        PhoneVerifyCodeSendDTO dto = new PhoneVerifyCodeSendDTO();
        dto.setPhone("13800138000");
        dto.setScene(VerifyCodeSceneEnum.LOGIN);
        String clientIp = "127.0.0.1";

        when(phoneCodeLimitCacheService.tryAcquireByIp(any(), anyString())).thenReturn(true);
        when(phoneCodeLimitCacheService.tryAcquire(any(), anyString())).thenReturn(false);

        BizException exception = assertThrows(
                BizException.class,
                () -> userService.sendPhoneVerifyCode(dto, clientIp),
                "手机号发送频率过高时应该抛出 BizException"
        );

        assertEquals(CommonErrorCode.PHONE_CODE_TOO_FREQUENT.getCode(), exception.getCode());
    }

    /**
     * 测试 sendPhoneVerifyCode 方法 - 正常情况
     */
    @Test
    void sendPhoneVerifyCode_ShouldSendCodeSuccessfully_WhenEverythingIsNormal() {
        PhoneVerifyCodeSendDTO dto = new PhoneVerifyCodeSendDTO();
        dto.setPhone("13800138000");
        dto.setScene(VerifyCodeSceneEnum.LOGIN);
        String clientIp = "127.0.0.1";

        when(phoneCodeLimitCacheService.tryAcquireByIp(any(), anyString())).thenReturn(true);
        when(phoneCodeLimitCacheService.tryAcquire(any(), anyString())).thenReturn(true);

        // 执行测试，应该正常返回，不抛出异常
        assertDoesNotThrow(
                () -> userService.sendPhoneVerifyCode(dto, clientIp),
                "正常情况下不应该抛出异常"
        );

        // 验证调用了保存验证码的方法
        verify(phoneCodeCacheService).saveCode(any(), anyString(), anyString());
    }
}
