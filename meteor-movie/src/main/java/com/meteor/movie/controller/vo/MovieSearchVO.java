package com.meteor.movie.controller.vo;

import com.meteor.api.enums.MovieStatusEnum;

import java.time.LocalDate;
import java.util.List;

/**
 *  搜索电影VO
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-14 15:33
 */
public record MovieSearchVO(
        String movieId,
        String title,
        String alias,
        Integer durationMin,
        LocalDate releaseDate,
        MovieStatusEnum status,
        String posterUrl,
        List<String> categories
) {}
