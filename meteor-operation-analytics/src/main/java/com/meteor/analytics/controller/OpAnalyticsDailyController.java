package com.meteor.analytics.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.meteor.analytics.controller.vo.PayTodayVO;
import com.meteor.analytics.controller.vo.RegisterTrend7dVO;
import com.meteor.analytics.controller.vo.Trend7dVO;
import com.meteor.analytics.controller.vo.TrendVO;
import com.meteor.analytics.service.IOpAnalyticsDailyService;
import com.meteor.common.result.Result;
import com.meteor.satoken.constants.RoleConst;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 运营分析-每日KPI汇总 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-11
 */
@SaCheckRole(RoleConst.ADMIN)
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


    @Operation(summary = "近7日支付尝试趋势(pay_attempt_cnt)")
    @GetMapping("/trend7d-pay")
    public Result<Trend7dVO> payTrend7d() {
        return Result.success(dailyService.getPayAttemptTrend7dGlobal());
    }

    @Operation(summary = "今日支付汇总(尝试/成功/成交/GMV)")
    @GetMapping("/today")
    public Result<PayTodayVO> today() {
        return Result.success(dailyService.getPayTodayGlobal());
    }

    @Operation(summary = "交易额趋势(GMV) - 按天(分)")
    @GetMapping("/trend")
    public Result<TrendVO> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(dailyService.getGmvTrendGlobal(days));
    }

}
