package com.meteor.movie.mapper;

import com.meteor.movie.domain.entity.Movie;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meteor.movie.mapper.row.MovieIdTitleRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 电影信息表 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface MovieMapper extends BaseMapper<Movie> {

    List<MovieIdTitleRow> selectIdTitleByIds(@Param("ids") List<Long> ids);
}
