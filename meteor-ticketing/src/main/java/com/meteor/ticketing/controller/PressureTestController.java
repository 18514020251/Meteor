package com.meteor.ticketing.controller;

/**
 * @author Programmer
 * @version 1.0
 * @date 2026-02-18 12:09
 */

import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import com.meteor.ticketing.controller.dto.PressureStartDTO;
import com.meteor.ticketing.controller.vo.PressureStartVO;
import com.meteor.ticketing.service.IPressureTaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端压测入口（创建压测任务）
 */
@RestController
@RequestMapping("/admin/pressure")
@RequiredArgsConstructor
@Validated
@SaCheckRole(RoleConst.ADMIN)
public class PressureTestController {

    private final IPressureTaskService pressureTaskService;

    @Operation(summary = "创建压测任务")
    @PostMapping("/start")
    public Result<PressureStartVO> start(@RequestBody @Valid PressureStartDTO req) {
        PressureStartVO resp = pressureTaskService.start(req);
        return Result.success(resp);
    }
}
