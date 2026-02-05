package com.meteor.movie.service.impl;

import com.meteor.movie.mapper.MediaAssetMapper;
import com.meteor.movie.mapper.MovieCategoryMapper;
import com.meteor.movie.mapper.MovieMapper;
import com.meteor.movie.mapper.row.MovieCategoryNameRow;
import com.meteor.movie.mapper.row.MovieIdTitleRow;
import com.meteor.movie.mapper.row.MoviePosterRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 电影查询实现类
 *
 * @author Programmer
 * @date 2026-02-04 15:23
 */
@Service
@RequiredArgsConstructor
public class MovieQueryServiceImpl {

    private final MovieMapper movieMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final MovieCategoryMapper movieCategoryMapper;

    public Map<Long, String> getTitleMap(List<Long> movieIds) {

        if (movieIds == null || movieIds.isEmpty()) {
            return Map.of();
        }

        return movieMapper.selectIdTitleByIds(movieIds)
                .stream()
                .collect(Collectors.toMap(
                        MovieIdTitleRow::id,
                        MovieIdTitleRow::title,
                        (a, b) -> a
                ));
    }

    public Map<Long, String> getPosterObjectKeyMap(List<Long> movieIds) {

        if (movieIds == null || movieIds.isEmpty()) {
            return Map.of();
        }

        return mediaAssetMapper.selectPosterObjectKeyByMovieIds(movieIds).stream()
                .collect(Collectors.toMap(
                        MoviePosterRow::movieId,
                        MoviePosterRow::objectKey,
                        (a, b) -> a
                ));
    }

    public Map<Long, List<String>> getCategoryNamesMap(List<Long> movieIds) {

        if (movieIds == null || movieIds.isEmpty()) {
            return Map.of();
        }

        List<MovieCategoryNameRow> rows =
                movieCategoryMapper.selectCategoryNamesByMovieIds(movieIds);

        Map<Long, List<String>> map = new HashMap<>();
        for (MovieCategoryNameRow row : rows) {
            map.computeIfAbsent(row.movieId(), k -> new ArrayList<>())
                    .add(row.categoryName());
        }
        return map;
    }

}

