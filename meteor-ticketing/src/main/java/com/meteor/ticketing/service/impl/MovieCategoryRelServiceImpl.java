package com.meteor.ticketing.service.impl;

import com.meteor.ticketing.domain.entity.MovieCategoryRel;
import com.meteor.ticketing.mapper.MovieCategoryRelMapper;
import com.meteor.ticketing.service.IMovieCategoryRelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 电影分类关联表 服务实现类
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
@Service
public class MovieCategoryRelServiceImpl extends ServiceImpl<MovieCategoryRelMapper, MovieCategoryRel> implements IMovieCategoryRelService {

}
