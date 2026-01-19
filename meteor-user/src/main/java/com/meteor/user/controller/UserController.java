package com.meteor.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.result.Result;
import com.meteor.user.domain.dto.UserLoginReq;
import com.meteor.user.domain.dto.UserProfileUpdateDTO;
import com.meteor.user.domain.dto.UserRegisterReq;
import com.meteor.user.domain.vo.UserInfoVO;
import com.meteor.user.service.IUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterReq req) {
        userService.register(req);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginReq req) {
        String token = userService.login(req);
        return Result.success(token);
    }

    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfoVO userInfo = userService.getCurrentUserInfo(userId);
        return Result.success(userInfo);
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        String avatarUrl = userService.uploadAvatar(file , userId);
        return Result.success(avatarUrl);
    }

    @DeleteMapping("")
    public Result<Void> deleteUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.deleteUserAndRelatedInfo(userId);
        return Result.success();
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfileUpdateDTO dto) {

        Long userId = StpUtil.getLoginIdAsLong();

        userService.updateProfile(userId, dto);

        return Result.success();
    }

}

