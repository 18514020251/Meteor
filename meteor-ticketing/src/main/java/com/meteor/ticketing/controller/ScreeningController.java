package com.meteor.ticketing.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import com.meteor.satoken.context.LoginContext;
import com.meteor.ticketing.controller.dto.screening.ScreeningCreateDTO;
import com.meteor.ticketing.service.IScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 电影场次表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Tag(name = "票务-场次")
@RestController
@RequestMapping("/ticketing/screenings")
@RequiredArgsConstructor
@Validated
public class ScreeningController {

    private final IScreeningService screeningService;
    private final LoginContext loginContext;

    @Operation(summary = "新增场次")
    @PostMapping
    @SaCheckRole(RoleConst.MERCHANT)
    public Result<Void> create(@RequestBody @Valid ScreeningCreateDTO dto) {
        Long uid = loginContext.currentLoginId();
        screeningService.create(uid , dto);
        return Result.success();
    }

}
