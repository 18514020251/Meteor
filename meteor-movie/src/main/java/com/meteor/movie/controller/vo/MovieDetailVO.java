package com.meteor.movie.controller.vo;

import com.meteor.api.enums.MovieStatusEnum;

import java.time.LocalDate;
import java.util.List;

/**
 *  电影详情 VO
 *
 * @author Programmer
 * @date 2026-02-08 9:37
 * @version 1.0
 */
public record MovieDetailVO(
        String id, // 电影ID
        String title, // 电影名称
        String alias, // 别名/英文名
        String intro, //  简介
        Integer durationMin, // 时长(分钟)
        LocalDate releaseDate, // 上映日期
        MovieStatusEnum status, // 状态
        List<String> categories, //  分类
        List<String> posters, // 海报
        Long serverTime // 服务器时间
) {}
