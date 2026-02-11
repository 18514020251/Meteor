package com.meteor.analytics.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 *  注册数趋势 VO
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 18:16
 */
@Schema(description = "近7日注册趋势")
public record RegisterTrend7dVO(
        @Schema(description = "日期(MM-dd)，长度固定7")
        List<String> dates,

        @Schema(description = "注册数，长度固定7")
        List<Integer> values
) {}
