package com.meteor.movie.service.impl;

import com.meteor.movie.domain.entity.MovieCategoryRel;
import com.meteor.movie.mapper.MovieCategoryRelMapper;
import com.meteor.movie.service.IMovieCategoryRelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meteor.movie.service.assembler.MovieCategoryRelAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 电影分类关联表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
@RequiredArgsConstructor
public class MovieCategoryRelServiceImpl extends ServiceImpl<MovieCategoryRelMapper, MovieCategoryRel> implements IMovieCategoryRelService {

    private final MovieCategoryRelAssembler assembler;

    @Override
    public void bindCategories(Long movieId, List<Long> categoryIds, Long operatorId, LocalDateTime now) {

        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        List<Long> distinctIds = categoryIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        for (Long categoryId : distinctIds) {
            MovieCategoryRel rel = assembler.create(movieId, categoryId, operatorId, now);
            baseMapper.insert(rel);
        }
    }
}
