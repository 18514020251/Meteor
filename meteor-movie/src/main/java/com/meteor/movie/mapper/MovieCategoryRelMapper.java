package com.meteor.movie.mapper;

import com.meteor.movie.domain.entity.MovieCategoryRel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 电影分类关联表 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface MovieCategoryRelMapper extends BaseMapper<MovieCategoryRel> {

    List<Long> selectMovieIdsByCategoryId(@Param("categoryId") Long categoryId,
                                          @Param("limit") Integer limit);
}
