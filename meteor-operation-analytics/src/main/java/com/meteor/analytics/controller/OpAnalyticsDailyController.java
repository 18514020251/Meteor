package com.meteor.analytics.controller;


import com.meteor.analytics.controller.vo.RegisterTrend7dVO;
import com.meteor.analytics.service.IOpAnalyticsDailyService;
import com.meteor.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 运营分析-每日KPI汇总 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
// NOTE:接口加鉴权
@RestController
@RequestMapping("/op-analytics/register")
@RequiredArgsConstructor
public class OpAnalyticsDailyController {

    private final IOpAnalyticsDailyService dailyService;

    @Operation(summary = "近7日注册趋势（GLOBAL）")
    @GetMapping("/trend7d")
    public Result<RegisterTrend7dVO> trend7d() {
        return Result.success(dailyService.queryRegisterTrend7dGlobal());
    }

}
