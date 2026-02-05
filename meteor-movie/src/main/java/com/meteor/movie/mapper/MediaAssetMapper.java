package com.meteor.movie.mapper;

import com.meteor.movie.domain.entity.MediaAsset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meteor.movie.mapper.row.MoviePosterRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 图片资源表 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface MediaAssetMapper extends BaseMapper<MediaAsset> {

    /**
     * 判断 objectKey 是否仍被使用（未删除）
     */
    @Select("""
        select 1
        from media_asset
        where object_key = #{objectKey}
          and deleted = 0
        limit 1
        """)
    Integer existsByObjectKey(String objectKey);

    List<MoviePosterRow> selectPosterObjectKeyByMovieIds(@Param("movieIds") List<Long> movieIds);

    @Select("""
        SELECT biz_id AS movieId, object_key AS objectKey, sort
        FROM media_asset
        WHERE deleted = 0
          AND biz_type = 1
          AND kind = 1
          AND biz_id IN (${ids})
        ORDER BY biz_id ASC, sort ASC
        """)
    List<MoviePosterRow> listMoviePosters(@Param("ids") String ids);
}
