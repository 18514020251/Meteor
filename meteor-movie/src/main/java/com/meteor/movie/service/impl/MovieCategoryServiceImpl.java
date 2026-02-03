package com.meteor.movie.service.impl;

import com.meteor.common.enums.system.DeleteStatus;
import com.meteor.movie.controller.vo.MovieCategoryVO;
import com.meteor.movie.domain.entity.MovieCategory;
import com.meteor.movie.mapper.MovieCategoryMapper;
import com.meteor.movie.service.IMovieCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 电影分类表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
public class MovieCategoryServiceImpl extends ServiceImpl<MovieCategoryMapper, MovieCategory> implements IMovieCategoryService {

    @Override
    public List<MovieCategoryVO> listAll() {

        return lambdaQuery()
                .select(MovieCategory::getId, MovieCategory::getName)
                .eq(MovieCategory::getDeleted, DeleteStatus.NORMAL)
                .orderByAsc(MovieCategory::getSort)
                .list()
                .stream()
                .map(c -> new MovieCategoryVO(c.getId(), c.getName()))
                .toList();
    }
}
