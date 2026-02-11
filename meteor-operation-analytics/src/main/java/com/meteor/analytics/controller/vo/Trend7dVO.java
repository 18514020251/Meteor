package com.meteor.analytics.controller.vo;

import java.util.List;

/**
 *  7日趋势
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-11 19:48
 */
public record Trend7dVO(
        List<String> dates,
        List<Integer> values
) {}
