package com.meteor.movie.mapper.row;

/**
 *  电影 id 分类
 *
 * @author Programmer
 * @date 2026-02-04 15:43
 */
public record MovieCategoryNameRow(
        Long movieId,
        String categoryName
) {}
