package com.meteor.movie.mapper;

import com.meteor.movie.domain.entity.MovieCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meteor.movie.mapper.row.MovieCategoryNameRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 电影分类表 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface MovieCategoryMapper extends BaseMapper<MovieCategory> {


    List<MovieCategoryNameRow> selectCategoryNamesByMovieIds(@Param("movieIds") List<Long> movieIds);

}
