package com.meteor.ticketing.mapper;

import com.meteor.ticketing.domain.entity.HotRank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 场次热度榜 Mapper 接口
 * </p>
 *
 * @author Programmer
 * @since 2026-02-02
 */
public interface HotRankMapper extends BaseMapper<HotRank> {

    @Update("""
    UPDATE hot_rank
    SET score = score + #{hot},
        update_time = NOW()
    WHERE screening_id = #{movieId}
    """)
    void increaseMovieHot(@Param("movieId") Long movieId,
                          @Param("hot") Long hot);
}
