package com.meteor.analytics.controller.vo;

import java.util.List;

/**
 *  金额交易趋势（7/30日）
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 20:18
 */
public record TrendVO(
        List<String> dates,
        List<Long> values
) {}
