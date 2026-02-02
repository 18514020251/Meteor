package com.meteor.movie.service.impl;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.movie.controller.dto.MovieCreateDTO;
import com.meteor.movie.domain.entity.Movie;
import com.meteor.movie.mapper.MovieMapper;
import com.meteor.movie.service.IMediaAssetService;
import com.meteor.movie.service.IMovieCategoryRelService;
import com.meteor.movie.service.IMovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.movie.service.assembler.MovieAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 电影信息表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */

@Service
@RequiredArgsConstructor
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements IMovieService {

    private final IMovieCategoryRelService movieCategoryRelService;
    private final IMediaAssetService mediaAssetService;
    private final MovieAssembler assembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMovie(MovieCreateDTO dto, Long operatorId) {

        LocalDateTime now = LocalDateTime.now();

        Movie movie = assembler.create(dto , operatorId, now);

        try {
            baseMapper.insert(movie);
        } catch (DuplicateKeyException e) {
            throw new BizException(CommonErrorCode.CONFLICT);
        } catch (Exception e) {
            throw new BizException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }

        Long movieId = movie.getId();

        movieCategoryRelService.bindCategories(movieId, dto.getCategoryIds(), operatorId, now);

        mediaAssetService.createForMovie(movieId, dto.getPosterKey(), dto.getCoverKey(), dto.getGalleryKeys(), operatorId, now);
    }
}

