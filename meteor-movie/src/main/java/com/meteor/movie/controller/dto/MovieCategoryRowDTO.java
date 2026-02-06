package com.meteor.movie.controller.dto;

import lombok.Data;

/**
 * 电影分类关联行数据传输对象
 *
 * @author Programmer
 * @date 2026-02-05 21:42
 */
@Data
public class MovieCategoryRowDTO {
    private Long movieId;
    private String categoryName;

    public MovieCategoryRowDTO() {}

    public MovieCategoryRowDTO(Long movieId, String categoryName) {
        this.movieId = movieId;
        this.categoryName = categoryName;
    }
}
