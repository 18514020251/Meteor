package com.meteor.movie.service.impl;

import com.meteor.movie.domain.entity.MovieCategory;
import com.meteor.movie.mapper.MovieCategoryMapper;
import com.meteor.movie.service.IMovieCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
