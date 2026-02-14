package com.meteor.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.admin.controller.dto.KickOutDTO;
import com.meteor.admin.controller.vo.OnlineUserVO;
import com.meteor.admin.service.IUserCacheService;
import com.meteor.common.constants.PageConstants;
import com.meteor.common.domain.PageResult;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import com.meteor.satoken.context.LoginContext;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.*;

/**
 *  在线用户管理
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-08 21:55
 */
@RestController
@RequestMapping("/admin/online-user")
@RequiredArgsConstructor
@SaCheckRole(RoleConst.ADMIN)
public class AdminOnlineUserController {

    private final IUserCacheService userCacheService;
    private final LoginContext loginContext;

    @Operation(summary = "分页查询在线用户")
    @GetMapping
    public Result<PageResult<OnlineUserVO>> pageOnlineUsers(
            @RequestParam(defaultValue = PageConstants.DEFAULT_PAGE_NUM + "") Integer pageNum
    ) {
        return Result.success(
                userCacheService.pageOnlineUsers(pageNum, PageConstants.ADMIN_FIXED_PAGE_SIZE)
        );
    }

    @Operation(summary = "踢出在线用户")
    @PostMapping("/kick")
    public Result<Void> kickOut(@RequestBody KickOutDTO kickOutDTO) {
        userCacheService.kickOutUser(kickOutDTO.getUserId());

        return Result.success();
    }
}
