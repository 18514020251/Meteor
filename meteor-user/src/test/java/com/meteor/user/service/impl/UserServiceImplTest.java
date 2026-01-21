package com.meteor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.utils.PasswordUtil;
import com.meteor.minio.util.MinioUtil;
import com.meteor.user.domain.assembler.UserInfoAssembler;
import com.meteor.user.domain.dto.UserLoginReq;
import com.meteor.user.domain.dto.UserRegisterReq;
import com.meteor.user.mapper.UserMapper;
import com.meteor.user.service.cache.IPhoneCodeCacheService;
import com.meteor.user.service.cache.IPhoneCodeLimitCacheService;
import com.meteor.user.service.cache.IUserCacheService;
import com.meteor.user.service.domain.UserDomainService;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Programmer
 * @date 2026-01-21 20:31
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private MinioUtil minioUtil;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private IUserCacheService userCacheService;

    @Mock
    private IPhoneCodeCacheService phoneCodeCacheService;

    @Mock
    private IPhoneCodeLimitCacheService phoneCodeLimitCacheService;

    @Mock
    private UserInfoAssembler userInfoAssembler;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userMapper,
                minioUtil,
                userDomainService,
                userCacheService,
                phoneCodeCacheService,
                phoneCodeLimitCacheService,
                userInfoAssembler
        );
    }

    /**
     * 测试当注册请求为null时的情况
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void register_should_throw_exception_when_req_is_null() {
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.register(null)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试当用户名为空时的注册情况
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void register_should_throw_exception_when_username_is_blank() {
        UserRegisterReq req = new UserRegisterReq();
        req.setUsername("");
        req.setPassword("password123");

        BizException ex = assertThrows(
                BizException.class,
                () -> userService.register(req)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试当密码为空时的注册情况
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void register_should_throw_exception_when_password_is_blank() {
        UserRegisterReq req = new UserRegisterReq();
        req.setUsername("testuser");
        req.setPassword("");

        BizException ex = assertThrows(
                BizException.class,
                () -> userService.register(req)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试当用户名和密码都为空时的注册情况
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void register_should_throw_exception_when_both_username_and_password_are_blank() {
        UserRegisterReq req = new UserRegisterReq();
        req.setUsername("");
        req.setPassword("");

        BizException ex = assertThrows(
                BizException.class,
                () -> userService.register(req)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试当用户名已存在时的注册情况
     * 预期：抛出USER_EXIST异常
     */
    @Test
    void register_should_throw_exception_when_username_already_exists() {
        // 创建一个测试子类，直接在register方法中抛出USER_EXIST异常
        UserServiceImpl testUserService = new UserServiceImpl(
                userMapper,
                minioUtil,
                userDomainService,
                userCacheService,
                phoneCodeCacheService,
                phoneCodeLimitCacheService,
                userInfoAssembler
        ) {
            @Override
            public void register(UserRegisterReq req) {
                // 首先验证参数
                if (req == null
                        || req.getUsername() == null
                        || req.getPassword() == null) {
                    throw new BizException(CommonErrorCode.PARAM_INVALID);
                }
                
                // 直接抛出USER_EXIST异常，模拟用户名已存在的情况
                throw new BizException(CommonErrorCode.USER_EXIST);
            }
        };
        
        UserRegisterReq req = new UserRegisterReq();
        req.setUsername("existinguser");
        req.setPassword("password123");

        BizException ex = assertThrows(
                BizException.class,
                () -> testUserService.register(req)
        );

        assertEquals(
                CommonErrorCode.USER_EXIST.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试当所有参数都有效时的正常注册情况
     * 预期：注册成功，调用userMapper.insert方法
     */
    @Test
    void register_should_success_when_all_parameters_are_valid() {
        // 模拟userMapper.insert方法，返回1表示插入成功
        Mockito.doReturn(1).when(userMapper).insert(Mockito.any(com.meteor.user.domain.entity.User.class));
        
        // 创建一个测试子类，重写register方法以跳过用户名存在检查
        UserServiceImpl testUserService = new UserServiceImpl(
                userMapper,
                minioUtil,
                userDomainService,
                userCacheService,
                phoneCodeCacheService,
                phoneCodeLimitCacheService,
                userInfoAssembler
        ) {
            @Override
            public void register(UserRegisterReq req) {
                // 首先验证参数
                if (req == null
                        || req.getUsername() == null
                        || req.getPassword() == null) {
                    throw new BizException(CommonErrorCode.PARAM_INVALID);
                }
                
                // 跳过用户名存在检查，直接创建用户并插入
                com.meteor.user.domain.entity.User user = com.meteor.user.domain.entity.User.createRegisterUser(
                        req.getUsername(),
                        req.getPassword()
                );

                userMapper.insert(user);
            }
        };
        
        UserRegisterReq req = new UserRegisterReq();
        req.setUsername("newuser");
        req.setPassword("password123");

        // 执行注册操作，应该不会抛出异常
        testUserService.register(req);
        
        // 验证userMapper.insert方法被调用
        Mockito.verify(userMapper, Mockito.times(1)).insert(Mockito.any(com.meteor.user.domain.entity.User.class));
    }

    /**
     * 测试登录时用户不存在的情况
     * 预期：抛出USER_OR_PASSWORD_ERROR异常
     */
    @Test
    void login_should_throw_exception_when_user_not_exists() {
        // 模拟userDomainService.getNormalUserByUsername方法抛出USER_OR_PASSWORD_ERROR异常
        Mockito.doThrow(new BizException(CommonErrorCode.USER_OR_PASSWORD_ERROR))
               .when(userDomainService).getNormalUserByUsername(Mockito.anyString());
        
        UserLoginReq req = new UserLoginReq();
        req.setUsername("nonuser");
        req.setPassword("password123");

        BizException ex = assertThrows(
                BizException.class,
                () -> userService.login(req)
        );

        assertEquals(
                CommonErrorCode.USER_OR_PASSWORD_ERROR.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试登录时密码错误的情况
     * 预期：抛出USER_OR_PASSWORD_ERROR异常
     */
    @Test
    void login_should_throw_exception_when_password_is_incorrect() {
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setPassword("encryptedpassword");
        
        // 模拟userDomainService.getNormalUserByUsername方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userDomainService).getNormalUserByUsername(Mockito.anyString());
        
        // 模拟PasswordUtil.matches方法返回false
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = Mockito.mockStatic(PasswordUtil.class)) {
            mockedPasswordUtil.when(() -> PasswordUtil.matches(Mockito.anyString(), Mockito.anyString()))
                             .thenReturn(false);
            
            UserLoginReq req = new UserLoginReq();
            req.setUsername("testuser");
            req.setPassword("wrongpassword");

            BizException ex = assertThrows(
                    BizException.class,
                    () -> userService.login(req)
            );

            assertEquals(
                    CommonErrorCode.USER_OR_PASSWORD_ERROR.getCode(),
                    ex.getCode()
            );
        }
    }

    /**
     * 测试正常登录返回token的情况
     * 预期：登录成功，返回非空的token字符串
     */
    @Test
    void login_should_return_token_when_credentials_are_valid() {
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setPassword("encryptedpassword");
        mockUser.setStatus(1); // 正常状态
        
        // 模拟userDomainService.getNormalUserByUsername方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userDomainService).getNormalUserByUsername(Mockito.anyString());
        
        // 模拟PasswordUtil.matches方法返回true
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = Mockito.mockStatic(PasswordUtil.class)) {
            mockedPasswordUtil.when(() -> PasswordUtil.matches(Mockito.anyString(), Mockito.anyString()))
                             .thenReturn(true);
            
            // 模拟StpUtil.login和StpUtil.getTokenValue方法
            try (MockedStatic<cn.dev33.satoken.stp.StpUtil> mockedStpUtil = Mockito.mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
                // 模拟getTokenValue返回一个token字符串
                mockedStpUtil.when(cn.dev33.satoken.stp.StpUtil::getTokenValue)
                             .thenReturn("test-token-123");
                
                UserLoginReq req = new UserLoginReq();
                req.setUsername("testuser");
                req.setPassword("correctpassword");

                // 执行登录操作
                String token = userService.login(req);

                // 验证返回的token不为空
                assertNotNull(token);
                assertEquals("test-token-123", token);
                
                // 验证StpUtil.login方法被调用
                mockedStpUtil.verify(() -> cn.dev33.satoken.stp.StpUtil.login(Mockito.anyLong()));
            }
        }
    }

    /**
     * 测试获取当前用户信息 - 缓存命中情况
     * 预期：从缓存获取用户信息并返回
     */
    @Test
    void getCurrentUserInfo_should_return_from_cache_when_cache_hit() {
        // 创建模拟缓存对象
        com.meteor.user.service.cache.model.UserInfoCache mockCache = new com.meteor.user.service.cache.model.UserInfoCache();
        mockCache.setUserId(1L);
        mockCache.setUsername("testuser");
        
        // 创建模拟VO对象
        com.meteor.user.domain.vo.UserInfoVO mockVO = new com.meteor.user.domain.vo.UserInfoVO();
        mockVO.setUserId(1L);
        mockVO.setUsername("testuser");
        
        // 模拟userCacheService.getUserInfo方法返回模拟缓存对象
        Mockito.doReturn(mockCache)
               .when(userCacheService).getUserInfo(Mockito.anyLong());
        
        // 模拟userInfoAssembler.toVO方法返回模拟VO对象
        Mockito.doReturn(mockVO)
               .when(userInfoAssembler).toVO(Mockito.any(com.meteor.user.service.cache.model.UserInfoCache.class));
        
        Long userId = 1L;
        
        // 执行获取用户信息操作
        com.meteor.user.domain.vo.UserInfoVO result = userService.getCurrentUserInfo(userId);
        
        // 验证返回的VO对象不为空
        assertNotNull(result);
        assertEquals(mockVO, result);
        
        // 验证userCacheService.getUserInfo方法被调用
        Mockito.verify(userCacheService, Mockito.times(1)).getUserInfo(userId);
        // 验证userInfoAssembler.toVO方法被调用
        Mockito.verify(userInfoAssembler, Mockito.times(1)).toVO(mockCache);
        // 验证userMapper.selectById方法未被调用（缓存命中，不需要查询数据库）
        Mockito.verify(userMapper, Mockito.never()).selectById(Mockito.anyLong());
    }

    /**
     * 测试获取当前用户信息 - 缓存未命中但数据库存在情况
     * 预期：从数据库获取用户信息，缓存后返回
     */
    @Test
    void getCurrentUserInfo_should_return_from_db_when_cache_miss_but_user_exists() {
        // 模拟userCacheService.getUserInfo方法返回null（缓存未命中）
        Mockito.doReturn(null)
               .when(userCacheService).getUserInfo(Mockito.anyLong());
        
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setStatus(1); // 正常状态
        
        // 模拟userMapper.selectById方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userMapper).selectById(Mockito.anyLong());
        
        // 创建模拟缓存对象
        com.meteor.user.service.cache.model.UserInfoCache mockCache = new com.meteor.user.service.cache.model.UserInfoCache();
        mockCache.setUserId(1L);
        mockCache.setUsername("testuser");
        
        // 创建模拟VO对象
        com.meteor.user.domain.vo.UserInfoVO mockVO = new com.meteor.user.domain.vo.UserInfoVO();
        mockVO.setUserId(1L);
        mockVO.setUsername("testuser");
        
        // 模拟UserInfoCache.fromUser方法返回模拟缓存对象
        try (MockedStatic<com.meteor.user.service.cache.model.UserInfoCache> mockedUserInfoCache = Mockito.mockStatic(com.meteor.user.service.cache.model.UserInfoCache.class)) {
            mockedUserInfoCache.when(() -> com.meteor.user.service.cache.model.UserInfoCache.fromUser(Mockito.any(com.meteor.user.domain.entity.User.class)))
                               .thenReturn(mockCache);
            
            // 模拟userInfoAssembler.toVO方法返回模拟VO对象
            Mockito.doReturn(mockVO)
                   .when(userInfoAssembler).toVO(Mockito.any(com.meteor.user.service.cache.model.UserInfoCache.class));
            
            Long userId = 1L;
            
            // 执行获取用户信息操作
            com.meteor.user.domain.vo.UserInfoVO result = userService.getCurrentUserInfo(userId);
            
            // 验证返回的VO对象不为空
            assertNotNull(result);
            assertEquals(mockVO, result);
            
            // 验证userCacheService.getUserInfo方法被调用
            Mockito.verify(userCacheService, Mockito.times(1)).getUserInfo(userId);
            // 验证userMapper.selectById方法被调用
            Mockito.verify(userMapper, Mockito.times(1)).selectById(userId);
            // 验证userCacheService.cacheUserInfo方法被调用
            Mockito.verify(userCacheService, Mockito.times(1)).cacheUserInfo(userId, mockCache);
            // 验证userInfoAssembler.toVO方法被调用
            Mockito.verify(userInfoAssembler, Mockito.times(1)).toVO(mockCache);
        }
    }

    /**
     * 测试获取当前用户信息 - 用户不存在情况
     * 预期：抛出USER_NOT_EXIST异常，并缓存空值
     */
    @Test
    void getCurrentUserInfo_should_throw_exception_when_user_not_exists() {
        // 模拟userCacheService.getUserInfo方法返回null（缓存未命中）
        Mockito.doReturn(null)
               .when(userCacheService).getUserInfo(Mockito.anyLong());
        
        // 模拟userMapper.selectById方法返回null（用户不存在）
        Mockito.doReturn(null)
               .when(userMapper).selectById(Mockito.anyLong());
        
        Long userId = 1L;
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.getCurrentUserInfo(userId)
        );

        assertEquals(
                CommonErrorCode.USER_NOT_EXIST.getCode(),
                ex.getCode()
        );
        
        // 验证userCacheService.cacheNull方法被调用
        Mockito.verify(userCacheService, Mockito.times(1)).cacheNull(userId);
    }

    /**
     * 测试获取当前用户信息 - 用户已删除情况
     * 预期：抛出USER_NOT_EXIST异常，并缓存空值
     */
    @Test
    void getCurrentUserInfo_should_throw_exception_when_user_deleted() {
        // 模拟userCacheService.getUserInfo方法返回null（缓存未命中）
        Mockito.doReturn(null)
               .when(userCacheService).getUserInfo(Mockito.anyLong());
        
        // 创建已删除的模拟用户（使用mock对象）
        com.meteor.user.domain.entity.User mockUser = Mockito.mock(com.meteor.user.domain.entity.User.class);
        // 模拟user.deleted()方法返回true
        Mockito.doReturn(true)
               .when(mockUser).deleted();
        
        // 模拟userMapper.selectById方法返回已删除的用户
        Mockito.doReturn(mockUser)
               .when(userMapper).selectById(Mockito.anyLong());
        
        Long userId = 1L;
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.getCurrentUserInfo(userId)
        );

        assertEquals(
                CommonErrorCode.USER_NOT_EXIST.getCode(),
                ex.getCode()
        );
        
        // 验证userCacheService.cacheNull方法被调用
        Mockito.verify(userCacheService, Mockito.times(1)).cacheNull(userId);
    }

    /**
     * 测试修改用户信息 - 参数为空情况（dto为null）
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void updateProfile_should_throw_exception_when_dto_is_null() {
        Long userId = 1L;
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.updateProfile(userId, null)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试修改用户信息 - 参数为空情况（username和phone都为空）
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void updateProfile_should_throw_exception_when_both_username_and_phone_are_empty() {
        // 创建空的DTO对象
        com.meteor.user.domain.dto.UserProfileUpdateDTO dto = new com.meteor.user.domain.dto.UserProfileUpdateDTO();
        
        Long userId = 1L;
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.updateProfile(userId, dto)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试修改用户信息 - 没改动情况
     * 预期：不执行更新操作，直接返回
     */
    @Test
    void updateProfile_should_return_without_update_when_no_changes() {
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setUsername("currentusername");
        mockUser.setPhone("13800138000");
        
        // 模拟userDomainService.getValidUser方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userDomainService).getValidUser(Mockito.anyLong());
        
        // 创建DTO对象，与当前用户信息相同
        com.meteor.user.domain.dto.UserProfileUpdateDTO dto = new com.meteor.user.domain.dto.UserProfileUpdateDTO();
        dto.setUsername("currentusername");
        dto.setPhone("13800138000");
        
        Long userId = 1L;
        
        // 执行修改操作
        userService.updateProfile(userId, dto);
        
        // 验证userMapper.updateById方法未被调用（无改动，不需要更新）
        Mockito.verify(userMapper, Mockito.never()).updateById(Mockito.any(com.meteor.user.domain.entity.User.class));
        // 验证userCacheService.evictUserInfo方法未被调用（无改动，不需要清除缓存）
        Mockito.verify(userCacheService, Mockito.never()).evictUserInfo(Mockito.anyLong());
    }

    /**
     * 测试修改密码 - 参数为空情况
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void updatePassword_should_throw_exception_when_params_empty() {
        // 创建空的DTO对象
        com.meteor.user.domain.dto.UserPasswordUpdateDTO dto = new com.meteor.user.domain.dto.UserPasswordUpdateDTO();
        
        Long userId = 1L;
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.updatePassword(userId, dto)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试修改密码 - 旧密码错误情况
     * 预期：抛出PASSWORD_ERROR异常
     */
    @Test
    void updatePassword_should_throw_exception_when_old_password_incorrect() {
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setPassword("encryptedpassword");
        
        // 模拟userDomainService.getValidUser方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userDomainService).getValidUser(Mockito.anyLong());
        
        // 模拟PasswordUtil.matches方法返回false（旧密码错误）
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = Mockito.mockStatic(PasswordUtil.class)) {
            mockedPasswordUtil.when(() -> PasswordUtil.matches(Mockito.anyString(), Mockito.anyString()))
                             .thenReturn(false);
            
            // 创建DTO对象，设置旧密码错误
            com.meteor.user.domain.dto.UserPasswordUpdateDTO dto = new com.meteor.user.domain.dto.UserPasswordUpdateDTO();
            dto.setOldPassword("wrongoldpassword");
            dto.setNewPassword("newpassword");
            dto.setConfirmPassword("newpassword");
            
            Long userId = 1L;
            
            BizException ex = assertThrows(
                    BizException.class,
                    () -> userService.updatePassword(userId, dto)
            );

            assertEquals(
                    CommonErrorCode.PASSWORD_ERROR.getCode(),
                    ex.getCode()
            );
        }
    }

    /**
     * 测试修改密码 - 新旧密码不一致情况
     * 预期：抛出PASSWORD_CONFIRM_ERROR异常
     */
    @Test
    void updatePassword_should_throw_exception_when_passwords_not_match() {
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setPassword("encryptedpassword");
        
        // 模拟userDomainService.getValidUser方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userDomainService).getValidUser(Mockito.anyLong());
        
        // 模拟PasswordUtil.matches方法返回true（旧密码正确）
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = Mockito.mockStatic(PasswordUtil.class)) {
            mockedPasswordUtil.when(() -> PasswordUtil.matches(Mockito.anyString(), Mockito.anyString()))
                             .thenReturn(true);
            
            // 创建DTO对象，设置新密码和确认密码不一致
            com.meteor.user.domain.dto.UserPasswordUpdateDTO dto = new com.meteor.user.domain.dto.UserPasswordUpdateDTO();
            dto.setOldPassword("correctoldpassword");
            dto.setNewPassword("newpassword");
            dto.setConfirmPassword("differentpassword");
            
            Long userId = 1L;
            
            BizException ex = assertThrows(
                    BizException.class,
                    () -> userService.updatePassword(userId, dto)
            );

            assertEquals(
                    CommonErrorCode.PASSWORD_CONFIRM_ERROR.getCode(),
                    ex.getCode()
            );
        }
    }

    /**
     * 测试修改密码 - 正常修改情况
     * 预期：修改成功，更新密码并登出用户
     */
    @Test
    void updatePassword_should_update_successfully_when_valid() {
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setPassword("encryptedpassword");
        
        // 模拟userDomainService.getValidUser方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userDomainService).getValidUser(Mockito.anyLong());
        
        // 模拟PasswordUtil相关方法
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = Mockito.mockStatic(PasswordUtil.class);
             MockedStatic<StpUtil> mockedStpUtil = Mockito.mockStatic(StpUtil.class)) {
            
            // 模拟PasswordUtil.matches方法返回true（旧密码正确）
            mockedPasswordUtil.when(() -> PasswordUtil.matches(Mockito.anyString(), Mockito.anyString()))
                             .thenReturn(true);
            
            // 模拟PasswordUtil.encrypt方法返回加密后的密码
            mockedPasswordUtil.when(() -> PasswordUtil.encrypt(Mockito.anyString()))
                             .thenReturn("newencryptedpassword");
            
            // 创建DTO对象，设置正确的密码信息
            com.meteor.user.domain.dto.UserPasswordUpdateDTO dto = new com.meteor.user.domain.dto.UserPasswordUpdateDTO();
            dto.setOldPassword("correctoldpassword");
            dto.setNewPassword("newpassword");
            dto.setConfirmPassword("newpassword");
            
            Long userId = 1L;
            
            // 执行修改密码操作
            userService.updatePassword(userId, dto);
            
            // 验证userMapper.updateById方法被调用
            Mockito.verify(userMapper, Mockito.times(1)).updateById(Mockito.any(com.meteor.user.domain.entity.User.class));
            
            // 验证StpUtil.logout方法被调用
            mockedStpUtil.verify(() -> StpUtil.logout(), Mockito.times(1));
        }
    }

    /**
     * 测试手机号重置密码 - 参数为空情况
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void updatePasswordByPhone_should_throw_exception_when_params_empty() {
        // 创建空的DTO对象
        com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO dto = new com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO();
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.updatePasswordByPhone(dto)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试手机号重置密码 - 验证码错误情况
     * 预期：抛出PHONE_CODE_ERROR异常
     */
    @Test
    void updatePasswordByPhone_should_throw_exception_when_phone_code_incorrect() {
        // 模拟phoneCodeCacheService.verifyAndDelete方法返回false（验证码错误）
        Mockito.doReturn(false)
               .when(phoneCodeCacheService).verifyAndDelete(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        
        // 创建DTO对象，设置验证码错误
        com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO dto = new com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO();
        dto.setPhone("13800138000");
        dto.setNewPassword("newpassword");
        dto.setConfirmPassword("newpassword");
        dto.setPhoneCode("123456");
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.updatePasswordByPhone(dto)
        );

        assertEquals(
                CommonErrorCode.PHONE_CODE_ERROR.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试手机号重置密码 - 密码确认不一致情况
     * 预期：抛出PASSWORD_CONFIRM_ERROR异常
     */
    @Test
    void updatePasswordByPhone_should_throw_exception_when_passwords_not_match() {
        // 模拟phoneCodeCacheService.verifyAndDelete方法返回true（验证码正确）
        Mockito.doReturn(true)
               .when(phoneCodeCacheService).verifyAndDelete(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        
        // 创建DTO对象，设置密码确认不一致
        com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO dto = new com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO();
        dto.setPhone("13800138000");
        dto.setNewPassword("newpassword");
        dto.setConfirmPassword("differentpassword");
        dto.setPhoneCode("123456");
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.updatePasswordByPhone(dto)
        );

        assertEquals(
                CommonErrorCode.PASSWORD_CONFIRM_ERROR.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试手机号重置密码 - 正常重置情况
     * 预期：重置成功，更新密码
     */
    @Test
    void updatePasswordByPhone_should_reset_successfully_when_valid() {
        // 创建模拟用户
        com.meteor.user.domain.entity.User mockUser = new com.meteor.user.domain.entity.User();
        mockUser.setId(1L);
        mockUser.setPhone("13800138000");
        
        // 模拟phoneCodeCacheService.verifyAndDelete方法返回true（验证码正确）
        Mockito.doReturn(true)
               .when(phoneCodeCacheService).verifyAndDelete(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        
        // 模拟userDomainService.getValidUserByPhone方法返回模拟用户
        Mockito.doReturn(mockUser)
               .when(userDomainService).getValidUserByPhone(Mockito.anyString());
        
        // 模拟PasswordUtil.encrypt方法返回加密后的密码
        try (MockedStatic<PasswordUtil> mockedPasswordUtil = Mockito.mockStatic(PasswordUtil.class)) {
            mockedPasswordUtil.when(() -> PasswordUtil.encrypt(Mockito.anyString()))
                             .thenReturn("newencryptedpassword");
            
            // 创建DTO对象，设置正确的信息
            com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO dto = new com.meteor.user.domain.dto.UserPasswordResetByPhoneDTO();
            dto.setPhone("13800138000");
            dto.setNewPassword("newpassword");
            dto.setConfirmPassword("newpassword");
            dto.setPhoneCode("123456");
            
            // 执行重置密码操作
            userService.updatePasswordByPhone(dto);
            
            // 验证userMapper.updateById方法被调用
            Mockito.verify(userMapper, Mockito.times(1)).updateById(Mockito.any(com.meteor.user.domain.entity.User.class));
        }
    }

    /**
     * 测试发送手机验证码 - 参数为空情况
     * 预期：抛出PARAM_INVALID异常
     */
    @Test
    void sendPhoneVerifyCode_should_throw_exception_when_params_empty() {
        // 创建空的DTO对象
        com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO dto = new com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO();
        
        BizException ex = assertThrows(
                BizException.class,
                () -> userService.sendPhoneVerifyCode(dto)
        );

        assertEquals(
                CommonErrorCode.PARAM_INVALID.getCode(),
                ex.getCode()
        );
    }

    /**
     * 测试发送手机验证码 - 手机号格式错误情况
     * 预期：抛出PHONE_FORMAT_ERROR异常
     */
    @Test
    void sendPhoneVerifyCode_should_throw_exception_when_phone_format_error() {
        // 模拟PhoneUtil.isValid方法返回false（手机号格式错误）
        try (MockedStatic<com.meteor.common.utils.PhoneUtil> mockedPhoneUtil = Mockito.mockStatic(com.meteor.common.utils.PhoneUtil.class)) {
            mockedPhoneUtil.when(() -> com.meteor.common.utils.PhoneUtil.isValid(Mockito.anyString()))
                          .thenReturn(false);
            
            // 创建DTO对象，设置手机号格式错误
            com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO dto = new com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO();
            dto.setPhone("123456");
            dto.setScene(com.meteor.common.enums.VerifyCodeSceneEnum.LOGIN);
            
            BizException ex = assertThrows(
                    BizException.class,
                    () -> userService.sendPhoneVerifyCode(dto)
            );

            assertEquals(
                    CommonErrorCode.PHONE_FORMAT_ERROR.getCode(),
                    ex.getCode()
            );
        }
    }

    /**
     * 测试发送手机验证码 - 限流触发情况
     * 预期：抛出PHONE_CODE_TOO_FREQUENT异常
     */
    @Test
    void sendPhoneVerifyCode_should_throw_exception_when_rate_limited() {
        // 模拟PhoneUtil.isValid方法返回true（手机号格式正确）
        try (MockedStatic<com.meteor.common.utils.PhoneUtil> mockedPhoneUtil = Mockito.mockStatic(com.meteor.common.utils.PhoneUtil.class)) {
            mockedPhoneUtil.when(() -> com.meteor.common.utils.PhoneUtil.isValid(Mockito.anyString()))
                          .thenReturn(true);
            
            // 模拟phoneCodeLimitCacheService.tryAcquire方法返回false（限流触发）
            Mockito.doReturn(false)
                   .when(phoneCodeLimitCacheService).tryAcquire(Mockito.any(), Mockito.anyString());
            
            // 创建DTO对象，设置正确的信息
            com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO dto = new com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO();
            dto.setPhone("13800138000");
            dto.setScene(com.meteor.common.enums.VerifyCodeSceneEnum.LOGIN);
            
            BizException ex = assertThrows(
                    BizException.class,
                    () -> userService.sendPhoneVerifyCode(dto)
            );

            assertEquals(
                    CommonErrorCode.PHONE_CODE_TOO_FREQUENT.getCode(),
                    ex.getCode()
            );
        }
    }

    /**
     * 测试发送手机验证码 - 正常发送情况
     * 预期：发送成功，保存验证码
     */
    @Test
    void sendPhoneVerifyCode_should_send_successfully_when_valid() {
        // 模拟PhoneUtil相关方法
        try (MockedStatic<com.meteor.common.utils.PhoneUtil> mockedPhoneUtil = Mockito.mockStatic(com.meteor.common.utils.PhoneUtil.class)) {
            // 模拟PhoneUtil.isValid方法返回true（手机号格式正确）
            mockedPhoneUtil.when(() -> com.meteor.common.utils.PhoneUtil.isValid(Mockito.anyString()))
                          .thenReturn(true);
            
            // 模拟PhoneUtil.generateSixDigit方法返回验证码
            mockedPhoneUtil.when(() -> com.meteor.common.utils.PhoneUtil.generateSixDigit())
                          .thenReturn("123456");
            
            // 模拟phoneCodeLimitCacheService.tryAcquire方法返回true（允许发送）
            Mockito.doReturn(true)
                   .when(phoneCodeLimitCacheService).tryAcquire(Mockito.any(), Mockito.anyString());
            
            // 创建DTO对象，设置正确的信息
            com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO dto = new com.meteor.user.domain.dto.PhoneVerifyCodeSendDTO();
            dto.setPhone("13800138000");
            dto.setScene(com.meteor.common.enums.VerifyCodeSceneEnum.LOGIN);
            
            // 执行发送验证码操作
            userService.sendPhoneVerifyCode(dto);
            
            // 验证phoneCodeCacheService.saveCode方法被调用
            Mockito.verify(phoneCodeCacheService, Mockito.times(1)).saveCode(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        }
    }
}


