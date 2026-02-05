package com.meteor.movie.mapper.row;

/**
 *  电影 id 海报
 *
 * @author Programmer
 * @date 2026-02-04 15:30
 */
public record MoviePosterRow(
        Long movieId,
        String objectKey
) {}
