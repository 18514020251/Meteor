package com.meteor.movie.controller.vo;

import java.util.List;

/**
 *  首页电影卡片 VO
 *
 * @author Programmer
 * @date 2026-02-04 12:35
 */
public record HomeMovieCardVO(
        Long movieId, // 电影id
        String title, // 电影名称
        String posterUrl,// 海报地址
        List<String>categories, //  分类
        Integer price, // 价格
        Boolean inGrabPeriod, // 是否处于抢票期间
        Integer hotScore // 热度
) {
}
