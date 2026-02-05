package com.meteor.movie.controller.vo;

import java.util.List;

/**
 *  内部数据传输类 主页
 *
 * @author Programmer
 * @date 2026-02-04 14:51
 */
public record HomeBaseInfo(
        Long movieId,
        String title,
        String posterObjectKey,
        List<String> categories
) {}

