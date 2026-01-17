package com.meteor.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.meteor.common.result.Result;
import com.meteor.user.domain.dto.UserLoginReq;
import com.meteor.user.domain.dto.UserRegisterReq;
import com.meteor.user.domain.vo.UserInfoVO;
import com.meteor.user.service.IUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "用户管理" , description = "用户管理")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody UserRegisterReq req) {
        userService.register(req);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody UserLoginReq req) {
        String token = userService.login(req);
        return Result.success(token);
    }

    @SaCheckLogin
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        //return Result.success(userService.getUserInfo(userId));
        return Result.success();
    }

}
