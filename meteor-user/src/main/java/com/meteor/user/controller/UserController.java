package com.meteor.user.controller;

import com.meteor.common.result.Result;
import com.meteor.common.utils.IpUtils;
import com.meteor.satoken.context.LoginContext;
import com.meteor.user.domain.dto.*;
import com.meteor.user.domain.vo.UserInfoVO;
import com.meteor.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-01-13
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户管理")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final LoginContext loginContext;

    @Operation(summary = "用户注册", description = "用户通过用户名、密码注册账号")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterReq req) {
        userService.register(req);
        return Result.success();
    }

    @Operation(summary = "用户登录", description = "用户通过用户名、密码进行登录")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginReq req) {
        String token = userService.login(req);
        return Result.success(token);
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人信息")
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        Long userId = loginContext.currentLoginId();
        UserInfoVO userInfo = userService.getCurrentUserInfo(userId);
        return Result.success(userInfo);
    }

    @Operation(summary = "上传用户头像", description = "用户上传并更新头像")
    @PutMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = loginContext.currentLoginId();
        String avatarUrl = userService.uploadAvatar(file , userId);
        return Result.success(avatarUrl);
    }

    @Operation(summary = "删除用户账号", description = "删除当前用户及其相关信息")
    @DeleteMapping("/delete")
    public Result<Void> deleteUser() {
        Long userId = loginContext.currentLoginId();
        userService.deleteUserAndRelatedInfo(userId);
        return Result.success();
    }

    @Operation(summary = "修改用户信息", description = "用户修改自己的用户名和手机号")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UserProfileUpdateDTO dto) {
        Long userId = loginContext.currentLoginId();
        userService.updateProfile(userId, dto);
        return Result.success();
    }

    @Operation(summary = "修改用户密码", description = "用户可以修改自己的密码")
    @PutMapping("/update-password")
    public Result<Void> updatePassword(@RequestBody @Valid UserPasswordUpdateDTO dto) {
        Long userId = loginContext.currentLoginId();
        userService.updatePassword(userId, dto);
        return Result.success();
    }

    @Operation(
            summary = "根据手机号修改密码",
            description = "通过手机号重置用户密码，不需要登录"
    )
    @PutMapping("/password/by-phone")
    public Result<Void> updatePasswordByPhone(
            @RequestBody @Valid UserPasswordResetByPhoneDTO dto) {

        userService.updatePasswordByPhone(dto);
        return Result.success();
    }

    @Operation(
            summary = "用户获取验证码" ,
            description = "用户获取手机验证码,用户绑定手机号"
    )
    @PostMapping("/phone/code")
    public Result<Void> sendPhoneVerifyCode(@Valid @RequestBody PhoneVerifyCodeSendDTO dto, HttpServletRequest  request) {
        String clientIp = IpUtils.getClientIp(request);
        userService.sendPhoneVerifyCode(dto , clientIp);
        return Result.success();
    }

}
